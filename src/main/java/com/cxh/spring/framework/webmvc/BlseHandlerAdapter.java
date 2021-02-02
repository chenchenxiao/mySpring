package com.cxh.spring.framework.webmvc;


import com.cxh.spring.framework.annotation.BlseRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cxh
 * @date: 2021/1/23
 * @description:
 */
public class BlseHandlerAdapter {

    public boolean support(Object handler) {
        return handler instanceof BlseHandlerMapping;
    }

    public BlseModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws InvocationTargetException, IllegalAccessException {
        BlseHandlerMapping handlerMapping = (BlseHandlerMapping) handler;
        //每个方法都有一个参数列表。这里保存的是形参列表
        //形参名称和对应的位置下标
        Map<String, Integer> paramMapping = new HashMap<String,Integer>();
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof BlseRequestParam) {
                    String paramName = ((BlseRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramMapping.put(paramName, i);
                    }
                }
            }
        }


        //根据用户请求的参数信息，跟method中的参数信息进行动态匹配
        //resp 传进来的目的是为了将其赋值给方法参数
        //只有当用户传过来的ModelAndView为空时，才会new一个默认的

        //1.准备好这个方法的形参列表
        //只处理Request和Response
        Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0 ; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramMapping.put(type.getName(), i);
            }
        }

        //2. 拿到自定义命名参数所在的位置
        //用户通过URL传过来的参数列表
        Map<String, String[]> reqParameterMap = req.getParameterMap();

        //3.构造实参列表
        Object[] paramValues = new Object[paramTypes.length];

        for (Map.Entry<String, String[]> param : reqParameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
            if (!paramMapping.containsKey(param.getKey())) {
                continue;
            }
            int index = paramMapping.get(param.getKey());

            //因为页面上传过来的值都是String类型，而在方法中定义的类型是千变万化的
            //对参数进行类型转换
            paramValues[index] = caseStringValue(value, paramTypes[index]);
        }
        if(paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }
        if(paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }
        //从handler中取出Controller，method，然后用反射机制进行调用
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);

        if (result == null) {
            return null;
        }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == BlseModelAndView.class;
        if (isModelAndView) {
            return (BlseModelAndView) result;
        } else {
            return null;
        }

    }

    private Object caseStringValue(String value,Class<?> clazz){
        if(clazz == String.class){
            return value;
        }else if(clazz == Integer.class){
            return Integer.valueOf(value);
        }else if(clazz == int.class){
            return Integer.valueOf(value).intValue();
        }else {
            return null;
        }
    }


}
