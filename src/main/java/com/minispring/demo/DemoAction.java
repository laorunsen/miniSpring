package com.minispring.demo;

import java.io.IOException;

import com.minispring.web.annotation.GPAutowired;
import com.minispring.web.annotation.GPController;
import com.minispring.web.annotation.GPRequestMapping;
import com.minispring.web.annotation.GPRequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@GPController
@GPRequestMapping("/demo")
public class DemoAction {
   @GPAutowired private IDemoService demoService;
   @GPRequestMapping("/query")
   public void query(HttpServletRequest req, HttpServletResponse resp,
                 @GPRequestParam("name") String name){
      String result = demoService.get(name);
      System.out.println(result);
      try {
         resp.getWriter().write(result);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   @GPRequestMapping("/add")
   public void add(HttpServletRequest req, HttpServletResponse resp,
               @GPRequestParam("a") Integer a, @GPRequestParam("b") Integer b){
      try {
         resp.getWriter().write(a + "+" + b + "=" + (a + b));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   @GPRequestMapping("/remove")
   public void remove(HttpServletRequest req,HttpServletResponse resp,
                  @GPRequestParam("id") Integer id){
   }
}
