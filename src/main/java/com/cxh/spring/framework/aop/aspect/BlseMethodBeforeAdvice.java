package com.cxh.spring.framework.aop.aspect;



import com.cxh.spring.framework.aop.intercept.BlseMethodInterceptor;
import com.cxh.spring.framework.aop.intercept.BlseMethodInvocation;

import java.lang.reflect.Method;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public class BlseMethodBeforeAdvice extends BlseAbstractAspectJAdvice implements BlseAdvice, BlseMethodInterceptor {

    private BlseJoinPoint joinPoint;

    public BlseMethodBeforeAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public void before(Method method, Object[] args, Object target) throws Throwable {
        invokeAdviceMethod(this.joinPoint, null, null);
    }

    @Override
    public Object invoke(BlseMethodInvocation mi) throws Throwable {
        this.joinPoint = mi;
        this.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
