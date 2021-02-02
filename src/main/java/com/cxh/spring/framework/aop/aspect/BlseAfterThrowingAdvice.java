package com.cxh.spring.framework.aop.aspect;



import com.cxh.spring.framework.aop.intercept.BlseMethodInterceptor;
import com.cxh.spring.framework.aop.intercept.BlseMethodInvocation;

import java.lang.reflect.Method;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public class BlseAfterThrowingAdvice extends BlseAbstractAspectJAdvice implements BlseAdvice, BlseMethodInterceptor {
    public BlseAfterThrowingAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private String throwingName;
    private BlseMethodInvocation mi;

    @Override
    public Object invoke(BlseMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (Throwable ex) {
            invokeAdviceMethod(mi, null, ex.getCause());
            throw ex;
        }
    }

    public void setThrowingName(String throwingName) {
        this.throwingName = throwingName;
    }
}
