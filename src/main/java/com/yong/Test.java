package com.yong;

import com.spring.YongApplicationContext;
import com.yong.service.UserInterFace;

public class Test {
    public static void main(String[] args) {

        YongApplicationContext context = new YongApplicationContext(AppConfig.class);

        UserInterFace userService = (UserInterFace) context.getBean("userService");
        userService.test();
    }
}
