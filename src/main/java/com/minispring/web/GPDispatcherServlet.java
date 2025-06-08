package com.minispring.web;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;

import com.minispring.web.annotation.GPAutowired;
import com.minispring.web.annotation.GPController;
import com.minispring.web.annotation.GPRequestMapping;
import com.minispring.web.annotation.GPRequestParam;
import com.minispring.web.annotation.GPService;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.io.File;

public class GPDispatcherServlet extends HttpServlet {
    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> ioc = new HashMap<String, Object>();
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();


    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
           doPost(req, resp);
        }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
            try {
                doDispatch(req, resp);
            } catch (Exception e) {
                e.printStackTrace();
                resp.getWriter().write("500 Exection,Detail : " + Arrays.toString(e.getStackTrace()));
            }
    }

    protected void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPaht = req.getContextPath();
        url = url.replace(contextPaht, "").replaceAll("/+", "/");

        if(!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = (Method)this.handlerMapping.get(url);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        Object[] paramValues = new Object[parameterTypes.length];
        Map<String, String[]> parameterMap = req.getParameterMap();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];

            if (paramType == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            }

            if (paramType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            }

            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof GPRequestParam) {
                    String paramName = ((GPRequestParam) annotation).value();
                    String[] values = parameterMap.get(paramName);

                    if (values != null && values.length > 0) {
                        String rawValue = values[0];
                        // 做类型转换
                        if (paramType == String.class) { 
                            paramValues[i] = rawValue;
                        } else if(paramType == Integer.class || paramType == int.class) {
                            paramValues[i] = Integer.valueOf(rawValue);
                        }else if(paramType == Double.class || paramType == double.class) {
                            paramValues[i] = Double.valueOf(rawValue);
                        }else if(paramType == Long.class || paramType == long.class) {
                            paramValues[i] = Long.valueOf(rawValue);
                        }
                    }
                }
            }
        }

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        Object controller = ioc.get(beanName);
        method.invoke(controller, paramValues);
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        doScanner(contextConfig.getProperty("scanPackage"));

        doInstance();

        doAutowired();

        initHandlerMapping();

        System.out.println("GP Spring framework is init.");
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream fis = null;
        try {
            fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doAutowired() {
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(GPAutowired.class)) {
                    continue;
                }
                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initHandlerMapping() {
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(GPController.class)){continue;}

            String baseUrl = "";
            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(GPRequestMapping.class)){continue;}

                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value())
                            .replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("Mapped " + url + " -> " + method);               
            }
        }
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader()
                    .getResource(scanPackage.replace(".", "/"));
        File classPath = new File(url.getFile());

        for(File file : classPath.listFiles()) {
            if(file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className =  scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doInstance() {
        try {
            for (String className : this.classNames) {
                Class<?> clazz = Class.forName(className);
                // controller 
                if (clazz.isAnnotationPresent(GPController.class)) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(GPService.class)) {
                    GPService service = clazz.getAnnotation(GPService.class);
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                       beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    ioc.put(beanName, instance);
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The “" + i.getName() + "” is exists!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}