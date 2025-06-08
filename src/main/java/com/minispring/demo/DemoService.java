package com.minispring.demo;

import com.minispring.web.annotation.Service;

@Service
public class DemoService implements IDemoService{
   public String get(String name) {
      return "My name is " + name;
   }
}
