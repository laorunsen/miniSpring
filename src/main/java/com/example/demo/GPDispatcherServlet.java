package com.example.demo;

import com.example.web.annotation.GPService;
import com.example.web.annotation.GPAutowired;
import com.example.web.annotation.GPController;
import com.example.web.annotation.GPRequestMapping;
import com.example.web.annotation.GPRequestParam;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.io.File;

public class GPDispatcherServlet extends HttpServlet {
    private Map<String, Object> mapping = new HashMap<>();
    private List<String> classNames = new ArrayList<>();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
           doPost(req, resp);
        }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
            try {
                doDispatch(req, resp);
            } catch (Exception e) {
                resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
            }
    }

    protected void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPaht = req.getContextPath();
        url = url.replace(contextPaht, "").replaceAll("/+", "/");

        if(!this.mapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = (Method)this.mapping.get(url);
        Object controller = this.mapping.get(method.getDeclaringClass().getName());

        Map<String, String[]> params = req.getParameterMap();
        List<Object> paramValues = new ArrayList<>();
        Class<?>[] paramTypes = method.getParameterTypes();

        for (Class<?> paramType : paramTypes) {
             if (paramType == HttpServletRequest.class) {
                paramValues.add(req);
            } else if (paramType == HttpServletResponse.class) {
                paramValues.add(resp);
            } else if (paramType == String.class) {
                String[] values = params.get("name");
                paramValues.add(values != null ? values[0] : null);
            } else {
                paramValues.add(null); // 不支持的参数填 null
            }
        }
        method.invoke(controller, paramValues.toArray());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
            // get applicationproperties file
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);

            String scanPackage = configContext.getProperty("scanPackage");
            doScanner(scanPackage);

            for (String className : this.classNames) {
                Class<?> clazz = Class.forName(className);
                // controller 
                if (clazz.isAnnotationPresent(GPController.class)) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    mapping.put(className, instance);

                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                        baseUrl = clazz.getAnnotation(GPRequestMapping.class).value();
                    }
                    for (Method method : clazz.getMethods()) {
                        if (!method.isAnnotationPresent(GPRequestMapping.class)) continue;
                         String methodUrl = method.getAnnotation(GPRequestMapping.class).value();
                         String fullUrl = (baseUrl + "/" + methodUrl).replaceAll("/+", "/");
                         mapping.put(fullUrl, method);
                         System.out.println("Mapped " + fullUrl + " -> " + method);
                    }
                } else if (clazz.isAnnotationPresent(GPService.class)) {
                    GPService service = clazz.getAnnotation(GPService.class);
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        beanName = clazz.getName();
                    }
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    mapping.put(beanName, instance);
                    for (Class<?> i : clazz.getInterfaces()) {
                        mapping.put(i.getName(), instance);
                    }
                }
            }
            // 注入 @Autowired
            for (Object object : mapping.values()) {
                if (object == null) continue;
                Class<?> clazz = object.getClass();
                if (clazz.isAnnotationPresent(GPController.class)) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (!field.isAnnotationPresent(GPAutowired.class)) continue;
                        String beanName = field.getAnnotation(GPAutowired.class).value();                       
                        if ("".equals(beanName)) {
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        field.set(object, mapping.get(beanName));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader()
                    .getResource(scanPackage.replace(".", "/"));
        File dir = new File(url.getFile());

        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className =  scanPackage + "." + file.getName().replace(".class", "");
                this.classNames.add(className);
            }
        }
    }

}