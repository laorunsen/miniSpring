package com.example.demo;

import com.example.web.annotation.GPService;

/**
 * 核心业务逻辑
 */
@GPService
public class DemoService implements IDemoService{
   public String get(String name) {
      return "My name is " + name;
   }
}
