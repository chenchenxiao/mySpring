package com.cxh.spring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public class BlseAbstractAspectJAdvice implements BlseAdvice{

    private Method aspectMethod;

    private Object aspectTarget;

    public BlseAbstractAspectJAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    protected Object invokeAdviceMethod(BlseJoinPoint joinPoint, Object returnValue, Throwable ex) throws Throwable{
        Class<?>[] paramsTypes = this.aspectMethod.getParameterTypes();
        if (null == paramsTypes || paramsTypes.length == 0) {
            return this.aspectMethod.invoke(aspectTarget);
        } else {
            Object[] args = new Object[paramsTypes.length];
            for (int i = 0; i < paramsTypes.length; i++) {
                if (paramsTypes[i] == BlseJoinPoint.class) {
                    args[i] = joinPoint;
                } else if (paramsTypes[i] == Throwable.class) {
                    args[i] = ex;
                } else if (paramsTypes[i] == Object.class) {
                    args[i] = returnValue;
                }
            }
            return this.aspectMethod.invoke(aspectTarget, args);
        }
    }




}
