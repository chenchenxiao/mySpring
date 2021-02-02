package com.cxh.spring.framework.webmvc;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author cxh
 * @date: 2021/1/23
 * @description:
 */

@Data
public class BlseHandlerMapping{

    private Object controller;

    private Method method;

    //url的封装
    private Pattern pattern;


    public BlseHandlerMapping(Pattern pattern, Object controller, Method method) {
        this.pattern = pattern;
        this.controller = controller;
        this.method = method;
    }
}
