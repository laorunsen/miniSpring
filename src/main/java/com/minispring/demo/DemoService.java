package com.minispring.demo;

import com.minispring.web.annotation.GPService;

/**
 * 核心业务逻辑
 */
@GPService
public class DemoService implements IDemoService{
   public String get(String name) {
      return "My name is " + name;
   }
}
