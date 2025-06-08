package com.minispring.boot;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import com.minispring.web.DispatcherServlet;
import java.io.File;

public class ContextApplication {
    private final Class<?> primarySource;

    public ContextApplication(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    public void run(String... args) throws Exception {
        // 加载应用配置
        loadApplicationProperties();
        
        // 创建并配置内嵌Tomcat
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(getPort());
        tomcat.getConnector();

        // 添加Web上下文
        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        // 注册DispatcherServlet
        Wrapper wrapper = Tomcat.addServlet(context, "dispatcherServlet", new DispatcherServlet());
        wrapper.addInitParameter("contextConfigLocation", "application.properties");
        context.addServletMappingDecoded("/", "dispatcherServlet");

        // 启动应用
        tomcat.start();
        System.out.println(primarySource.getSimpleName() + " started at http://localhost:" + getPort() + "/");
        tomcat.getServer().await();
    }

    private void loadApplicationProperties() {
		return;
    }

    private int getPort() {
        return 8080;
    }

    public static void run(Class<?> primarySource, String... args) {
        try {
            new ContextApplication(primarySource).run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}    