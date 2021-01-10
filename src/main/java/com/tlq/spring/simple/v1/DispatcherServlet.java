package com.tlq.spring.simple.v1;

import com.tlq.spring.simple.annotation.Autowired;
import com.tlq.spring.simple.annotation.Controller;
import com.tlq.spring.simple.annotation.RequestMapping;
import com.tlq.spring.simple.annotation.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description: DispatcherServlet
 * @Author: TanLinquan
 * @Date: 2021/1/10 20:23
 * @Version: V1.0
 **/
public class DispatcherServlet extends HttpServlet {

    /**
     * 用于创建方法映射
     */
    private final Map<String, Object> mapping = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
    }

    //init方法处理相关的从初始化工作

    /***
     * ioc
     * di
     * aop
     * mvc
     * */
    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream inputStream = null;
        try {
            //加载配置
            Properties configContext = new Properties();
            inputStream = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(inputStream);
            //获取需要扫描的包
            String scanPackage = configContext.getProperty("scanPackage");
            //根据scanpackage配置相关的信息
            doScanner(scanPackage);
            //IOC处理
            for (String className : mapping.keySet()) {
                if (!className.contains(".")) {
                    continue;
                }
                /**
                 * 分別处理contrller service
                 * */
                Class<?> clazz = Class.forName(className);
                //处理含有controller注解
                if (clazz.isAnnotationPresent(Controller.class)) {
                    mapping.put(className, clazz.newInstance());
                    //处理baseUrl
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    //处理controller下的方法
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        //当method上无相关的requestMapping注解则不进行处理
                        if (!method.isAnnotationPresent(RequestMapping.class)) {
                            continue;
                        }
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        String url = (baseUrl + "/" + requestMapping.value());
                        //放入mapping映射
                        mapping.put(url, method);
                        System.out.println("Mapper " + url + "," + method.getName());
                    }
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);
                    //处理service注入名称
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        beanName = clazz.getName();
                    }
                    //将相关实例注入mapping
                    Object instance = clazz.newInstance();
                    mapping.put(beanName, instance);
                    //注入相关bean实现的接口等
                    for (Class<?> i : clazz.getInterfaces()) {
                        mapping.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }

            //DI注入
            for (Object object : mapping.values()) {
                if (Objects.isNull(object)) {
                    continue;
                }
                Class clazz = object.getClass();
                //通过class来获取相关注解，并根据注解进行相关的注入处理
                if (clazz.isAnnotationPresent(Controller.class)) {
                    //获取相关字段
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        //判断字段会否有相关的Autowried注解
                        if (!field.isAnnotationPresent(Autowired.class)) {
                            continue;
                        }

                        Autowired autowired = field.getAnnotation(Autowired.class);
                        String beanName = autowired.value();
                        if ("".equals(beanName)) {
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        try {
                            field.set(mapping.get(clazz.getName()), mapping.get(beanName));

                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (
                ClassNotFoundException e) {
            e.printStackTrace();
        } catch (
                IllegalAccessException e) {
            e.printStackTrace();
        } catch (
                InstantiationException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.print("GP MVC Framework is init");
    }

    /**
     * 根据scanPackage获取相关类
     */
    private void doScanner(String scanPackage) {
        //获取文件url
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        //根据url获取相关的文件
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            //判断文件是否是文件夹
            if (file.isDirectory()) {
                //递归处理
                doScanner(scanPackage + "." + file.getName());
            } else { //处理文件（只处理.class文件）
                if (file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = (scanPackage + file.getName().replace(".class", ""));
                mapping.put(clazzName, null);
            }
        }
    }
}
