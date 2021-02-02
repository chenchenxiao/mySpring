package com.cxh.spring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public interface BlseJoinPoint {

    Method getMethod();

    Object[] getArguments();

    Object getThis();

    void setUserAttribute(String s, Object currentTimeMillis);

    Object getUserAttribute(String s);
}
