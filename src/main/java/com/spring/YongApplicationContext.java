package com.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YongApplicationContext {

     private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String,BeanDefinition>();

     private Map<String, Object> singletonObjets= new HashMap<String, Object>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    private Class appConfig;

    public YongApplicationContext(Class appConfig){
        this.appConfig=appConfig;
        //扫描
        scan(appConfig);

        for (Map.Entry<String, BeanDefinition> entry: beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())){
                //当前bean定义的类型singleton创建bean
                Object obj = createBean(beanName, beanDefinition);
                singletonObjets.put(beanName, obj);
            }
        }

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class type = beanDefinition.getType();
        Object newInstance = null;
        try {
            //默认无参构造方法实例化
            newInstance = type.getConstructor().newInstance();
            Field[] declaredFields = type.getDeclaredFields();
            for (Field field:declaredFields){
                //属性赋值
                if (field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    field.set(newInstance, getBean(field.getName()));
                }
            }

            if (newInstance instanceof BeanNameAware){
                ((BeanNameAware) newInstance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                newInstance = beanPostProcessor.postProcessBeforeInitialization(newInstance, beanName);
            }

            if (newInstance instanceof InitializingBean) {
                ((InitializingBean) newInstance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                newInstance = beanPostProcessor.postProcessAfterInitialization(newInstance, beanName);
            }


        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return newInstance;
    }

    public Object getBean(String beanName) {
        //beanDefinitionMap是否存在beanName
        if (!beanDefinitionMap.containsKey(beanName)){
            throw new NullPointerException();
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        //当前bean定义的类型为单列
        if ("singleton".equals(beanDefinition.getScope())){
            Object singletonBean = singletonObjets.get(beanName);
            if (singletonBean == null){
                singletonBean = createBean(beanName, beanDefinition);
                singletonObjets.put(beanName, singletonBean);
            }
            return singletonBean;
        }else {
            //原型bean
            Object protoTypeBean = createBean(beanName, beanDefinition);
            return protoTypeBean;
        }
    }

    private void scan(Class appConfig) {
        //判断当前类存在ComponentScan注解
        if (appConfig.isAnnotationPresent(ComponentScan.class)){
            //获取注解的信息
            ComponentScan componentScanAnnotation = (ComponentScan) appConfig.getAnnotation(ComponentScan.class);
            //注解的值
            String path = componentScanAnnotation.value();
            //替换路径
            path = path.replace(".","/");
            System.out.println(path);
            //类加载器
            ClassLoader classLoader = YongApplicationContext.class.getClassLoader();
            //获取路径下的文件
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());
            if (file.isDirectory()){
                for (File f:file.listFiles()){
                    //相对路径
                    String absolutePath = f.getAbsolutePath();
                    System.out.println(absolutePath);
                    //截取转换为包名
                    absolutePath = absolutePath.substring(absolutePath.indexOf("com"),absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("\\",".");
                    System.out.println(absolutePath);
                    try {
                        //加载类
                        Class<?> aClass = classLoader.loadClass(absolutePath);
                        if (aClass.isAnnotationPresent(Component.class)){

                            if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                                BeanPostProcessor instance = null;
                                try {
                                    instance = (BeanPostProcessor) aClass.getConstructor().newInstance();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                                beanPostProcessorList.add(instance);
                            }

                            Component componentAnnotation = aClass.getAnnotation(Component.class);
                            //获取当前类的名字 ，默认类名小写
                            String beaName = componentAnnotation.value();
                            if ("".equals(beaName)){
                                beaName = Introspector.decapitalize(aClass.getSimpleName());
                            }
                            //定义Bean
                            BeanDefinition beanDefinition = new BeanDefinition();
                            //类型
                            beanDefinition.setType(aClass);
                            if (aClass.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
                                String value = scopeAnnotation.value();
                                beanDefinition.setScope(value);
                            }else {
                                //默认单列
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinitionMap.put(beaName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
