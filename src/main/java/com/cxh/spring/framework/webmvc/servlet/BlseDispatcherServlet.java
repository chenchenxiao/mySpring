package com.cxh.spring.framework.webmvc.servlet;


import com.cxh.spring.framework.annotation.BlseController;
import com.cxh.spring.framework.annotation.BlseRequestMapping;
import com.cxh.spring.framework.context.BlseApplicationContext;
import com.cxh.spring.framework.webmvc.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cxh
 * @date: 2021/1/19
 * @description:
 */

public class BlseDispatcherServlet extends HttpServlet {

    private final String LOCATION = "contextConfigLocation";

    private List<BlseHandlerMapping> handlerMappings = new ArrayList<BlseHandlerMapping>();

    private Map<BlseHandlerMapping, BlseHandlerAdapter> handleAdapters = new HashMap<BlseHandlerMapping, BlseHandlerAdapter>();

    private List<BlseViewResolver> viewResolvers = new ArrayList<BlseViewResolver>();

    private BlseApplicationContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化IOC容器
        context = new BlseApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);
    }

    private void initStrategies(BlseApplicationContext context) {
        //九种策略，初始化九大组件

        //文件上传解析
        initMultipartResolver(context);
        //本地化解析
        initLocaleResolver(context);
        //主题解析
        initThemeResolver(context);
        //处理器映射器
        //用来保存Controller中配置的RequestMapping和Method的一个对应关系，将请求映射到处理器
        initHandlerMappings(context);
        //HandlerAdapters用来动态匹配Method参数，包括类型转换，动态赋值
        initHandlerAdapters(context);
        //异常解析器
        initHandlerExceptionResolvers(context);
        //解析请求到视图名
        initRequestToViewNameTranslator(context);
        //通过ViewResolvers实现动态模板的解析
        //自己解析一套模板语言,将逻辑视图解析到具体视图实现
        initViewResolvers(context);
        //flash映射管理器
        initFlashMapManager(context);
    }

    private void initViewResolvers(BlseApplicationContext context) {
        //解决页面名字和模板文件关联的问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new BlseViewResolver(templateRoot));
        }

    }

    private void initHandlerAdapters(BlseApplicationContext context) {
        //在初始化阶段，将这些参数的名字或类型按一定的顺序保存下来
        //因为后面用反射调用的时候，传的形参是一个数组
        //可以通过记录这些参数的位置index，挨个从数组会中填值，这样的话，就和参数顺序无关了
        for (BlseHandlerMapping handlerMapping : this.handlerMappings) {
            this.handleAdapters.put(handlerMapping, new BlseHandlerAdapter());
        }

    }

    //将Controller配置的RequestMapping和Method进行一一对应
    private void initHandlerMappings(BlseApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames) {
                //到了MVC层，对外提供的方法只有一个getBean方法
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(BlseController.class)) {
                    continue;
                }
                String baseUrl = "";

                if (clazz.isAnnotationPresent(BlseRequestMapping.class)) {
                    BlseRequestMapping requestMapping = clazz.getAnnotation(BlseRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                //扫描所有的public方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(BlseRequestMapping.class)) {
                        continue;
                    }
                    BlseRequestMapping requestMapping = method.getAnnotation(BlseRequestMapping.class);
                    String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*",
                            ".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new BlseHandlerMapping(pattern, controller, method));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initFlashMapManager(BlseApplicationContext context) {}
    private void initRequestToViewNameTranslator(BlseApplicationContext context) {}
    private void initHandlerExceptionResolvers(BlseApplicationContext context) {}
    private void initThemeResolver(BlseApplicationContext context) {}
    private void initLocaleResolver(BlseApplicationContext context) {}
    private void initMultipartResolver(BlseApplicationContext context) {}
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s","\r\n") + "<font color='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, IOException {
        //根据用户请求的URL获取到一个Handler
        BlseHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req,resp,new BlseModelAndView("404"));
            return;
        }
        //获取处理器
        BlseHandlerAdapter ha = getHandlerAdapter(handler);
        //调用方法，得到返回值
        BlseModelAndView mv = ha.handle(req, resp, handler);
        //输出
        processDispatchResult(req,resp, mv);
    }

    private BlseHandlerAdapter getHandlerAdapter(BlseHandlerMapping handler) {
        if (this.handleAdapters.isEmpty()) {
            return null;
        }
        BlseHandlerAdapter ha = this.handleAdapters.get(handler);
        if (ha.support(handler)) {
            return ha;
        }
        return null;
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, BlseModelAndView mv) throws IOException {
        if (null == mv) {
            return;
        }

        if (this.viewResolvers.isEmpty()) {
            return;
        }

        if (this.viewResolvers != null) {
            for (BlseViewResolver viewResolver : this.viewResolvers) {
                BlseView view = viewResolver.resolveViewName(mv.getViewName(), null);
                if (view != null) {
                    view.render(mv.getModel(), req, resp);
                    return;
                }
            }
        }
    }

    private BlseHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");
        for (BlseHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if(!matcher.matches()){ continue;}
            return handler;
        }
        return null;
    }

}
