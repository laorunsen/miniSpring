package com.minispring.demo;

import com.minispring.boot.ContextApplication;

public class DemoApplication {
    public static void main(String[] args) {
        // 通过ContextApplication启动应用
        ContextApplication.run(DemoApplication.class, args);
    }
} 