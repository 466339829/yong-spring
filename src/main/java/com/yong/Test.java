package com.yong;

import com.spring.YongApplicationContext;
import com.yong.service.UserService;

public class Test {
    public static void main(String[] args) {

        YongApplicationContext context = new YongApplicationContext(AppConfig.class);

        UserService userSevice = (UserService) context.getBean("userService");
        userSevice.test();
    }
}
