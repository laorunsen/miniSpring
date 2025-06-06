package com.example.demo;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import java.io.File;


public class Application {

	public static void main(String[] args) throws Exception {
		// 创建 Tomcat 实例
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8080);
		tomcat.getConnector();

		// 添加 Web 上下文（临时目录）
        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

		// 注册 Servlet
		Tomcat.addServlet(context, "helloServlet", new HelloServlet());
		context.addServletMappingDecoded("/hello", "helloServlet");

		tomcat.start();
		System.out.println("Mini Spring Boot started at http://localhost:8080/hello");
		tomcat.getServer().await();
	}

}
