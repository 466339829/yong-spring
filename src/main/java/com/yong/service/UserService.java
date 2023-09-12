package com.yong.service;

import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;

@Component
public class UserService implements UserInterFace, BeanNameAware {

    @Autowired
    private OrderService orderService;

    @Override
    public void test(){
        orderService.test();
        System.out.println("userService test");
    }

    private String beanName;

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
