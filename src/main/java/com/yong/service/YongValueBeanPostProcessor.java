package com.yong.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.Field;

@Component
public class YongValueBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(YongValue.class)) {
                field.setAccessible(true);
                try {
                    field.set(bean, field.getAnnotation(YongValue.class).value());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // bean
        return bean;
    }
}
