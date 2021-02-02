package com.cxh.spring.framework.aop.aspect;



import com.cxh.spring.framework.aop.intercept.BlseMethodInterceptor;
import com.cxh.spring.framework.aop.intercept.BlseMethodInvocation;

import java.lang.reflect.Method;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public class BlseAfterRunningAdvice extends BlseAbstractAspectJAdvice implements BlseAdvice, BlseMethodInterceptor {

    private BlseJoinPoint joinPoint;

    public BlseAfterRunningAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(BlseMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }

    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        method.invoke(target, returnValue);
    }
}
