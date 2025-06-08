package com.minispring.demo;

import java.io.IOException;

import com.minispring.web.annotation.Autowired;
import com.minispring.web.annotation.Controller;
import com.minispring.web.annotation.RequestMapping;
import com.minispring.web.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DemoAction {
   @Autowired private IDemoService demoService;
   @RequestMapping("/query")
   public void query(HttpServletRequest req, HttpServletResponse resp,
                 @RequestParam("name") String name){
      String result = demoService.get(name);
      System.out.println(result);
      try {
         resp.getWriter().write(result);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   @RequestMapping("/add")
   public void add(HttpServletRequest req, HttpServletResponse resp,
               @RequestParam("a") Integer a, @RequestParam("b") Integer b){
      try {
         resp.getWriter().write(a + "+" + b + "=" + (a + b));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   @RequestMapping("/remove")
   public void remove(HttpServletRequest req,HttpServletResponse resp,
                  @RequestParam("id") Integer id){
   }
}
