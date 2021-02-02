package com.cxh.spring.mvcframework.servlet;


import com.cxh.spring.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cxh
 * @date: 2021/1/7
 * @description:
 */
public class BlseDispathcerServlet extends HttpServlet {

    //保存application.properties配置文件中的内容
    private Properties contextConfig = new Properties();

    //保存扫描的所有的类名
    private List<String> classNames = new ArrayList<String>();

    //IOC容器
    private Map<String,Object> ioc = new HashMap<String,Object>();

    //思考：为什么不用Map
    //你用Map的话，key，只能是url
    //Handler 本身的功能就是把url和method对应关系，已经具备了Map的功能
    //根据设计原则：冗余的感觉了，单一职责，最少知道原则，帮助我们更好的理解
    private List<Handler> handlerMapping = new ArrayList<Handler>();

    //保存一个Handler和一个Method的关系
    public class Handler {

        private Pattern pattern;

        private Method method;

        private Object controller;

        private Class<?>[] paramTypes;

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public Method getMethod() {
            return method;
        }

        public Object getController() {
            return controller;
        }

        public Pattern getPattern() {
            return pattern;
        }

        private Map<String, Integer> paramIndexMapping;

        public Handler(Pattern pattern, Object controller, Method method) {
            this.pattern = pattern;
            this.method = method;
            this.controller = controller;

            paramTypes = method.getParameterTypes();

            paramIndexMapping = new HashMap<String, Integer>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {
            //提取方法中加了注解的参数
            //把方法上的注解拿到，得到的是一个二维数组
            //因为一个参数可以有多个注解，一个方法有多个参数
            Annotation[] [] pa = method.getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof BlseRequestParam) {
                        String paramName = ((BlseRequestParam)a).value();
                        if (!"".equals(paramName.trim())) {
                            paramIndexMapping.put(paramName, i);
                        }
                    }
                }
            }

            //提取方法中request和response参数
            Class<?>[] paramsTypes = method.getParameterTypes();
            for (int i = 0; i < paramsTypes.length; i++) {
                Class<?> type = paramsTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramIndexMapping.put(type.getName(), i);
                }
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch(Exception e) {
           e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Handler handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("404------");
            return;
        }
        Class<?>[] paramTypes = handler.getParamTypes();
        Object[] paramValues = new Object[paramTypes.length];
        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> parm : params.entrySet()) {
            String value = Arrays.toString(parm.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
            if (!handler.paramIndexMapping.containsKey(parm.getKey())) {
                continue;
            }
            int index = handler.paramIndexMapping.get(parm.getKey());
            paramValues[index] = convert(paramTypes[index], value);
        }

        if (handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int repIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[repIndex] = resp;
        }
        Object returnValue = handler.method.invoke(handler.controller, paramValues);
        if (returnValue == null || returnValue instanceof Void) {
            return;
        }
        resp.getWriter().write(returnValue.toString());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1  加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2  扫描
        doScanner(contextConfig.getProperty("scanPackage"));
        //3  初始化扫描到的类，放入IOC容器
        doInstance();
        //4  依赖注入
        doAutowired();
        //5  初始化HandlerMapping
        initHandlerMapping();

        System.out.println("init success-------");
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(BlseController.class)) {
                continue;
            }

            //保存类上面的requestMapping("/demo")
            String baseUrl = "";
            if (clazz.isAnnotationPresent(BlseRequestMapping.class)) {
                BlseRequestMapping requestMapping = clazz.getAnnotation(BlseRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //默认获取所有public方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(BlseRequestMapping.class)) {
                    continue;
                }
                BlseRequestMapping requestMapping = method.getAnnotation(BlseRequestMapping.class);
                //优化   //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                this.handlerMapping.add(new Handler(pattern, entry.getValue(), method));
                System.out.println("Mapped :" + pattern + "," + method);
            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //获取所有字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(BlseAutowired.class)) {
                    continue;
                }
                BlseAutowired autowired = field.getAnnotation(BlseAutowired.class);
                //如果用户没有自定义beanName则按默认类型注入
                //这里省去了对类名首字母小写的判断
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    //获取接口类型，作为key去IOC容器中取值
                    beanName = field.getType().getName();
                }
                //暴力访问
                field.setAccessible(true);
                try {
                    //利用反射给字段赋值
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        //初始化，为DI做准备
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                //初始化类,放入IOC容器
                if (clazz.isAnnotationPresent(BlseController.class)) {
                    Object instance = clazz.newInstance();
                    //默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(BlseService.class)) {
                    //1  自定义的beanName
                    BlseService service = clazz.getAnnotation(BlseService.class);
                    String beanName = service.value();
                    //2  默认首字母小写
                    if ("".equals(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //3  根据类型自动赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The" + i.getName() + "is exists!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {
        //scanPackage 存储的是包路径
        //转换为文件路径
        //classpath
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file: classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                classNames.add(className);
            }

        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        //直接从类路径下找到spring配置文件所在路径，
        //读取出来放到Properties对象
        //相对于scanpackage  从文件中保存到了内存中
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private Object convert(Class<?> type, String value) {
        if (Integer.class == type) {
            return Integer.valueOf(value);
        } else if(Double.class == type){
            return Double.valueOf(value);
        }
        return value;
    }

    private Handler getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) {
            return null;
        }
        //绝对路径
        String url = req.getRequestURI();
        //处理成相对路径
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (Handler handler : this.handlerMapping) {
            Pattern pattern = handler.getPattern();
            pattern.matcher(url);
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }
        return null;

    }

}
