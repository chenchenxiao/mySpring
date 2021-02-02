package com.cxh.spring.framework.aop.support;


import com.cxh.spring.framework.aop.BlseApoProxy;
import com.cxh.spring.framework.aop.intercept.BlseMethodInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public class BlseJdkDynamicAopProxy implements BlseApoProxy, InvocationHandler {

    private BlseAdviceSupport adviceSupport;

    public BlseJdkDynamicAopProxy(BlseAdviceSupport adviceSupport) {
        this.adviceSupport = adviceSupport;
    }


    @Override
    public Object getProxy() {
        return getProxy(this.adviceSupport.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        Object proxy = Proxy.newProxyInstance(classLoader, this.adviceSupport.getTargetClass().getInterfaces(), this);
        return proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicMethodMatchers = this.adviceSupport.getInterceptorsAndDynamicInterceptionAdvice(method,this.adviceSupport.getTargetClass());
        BlseMethodInvocation invocation = new BlseMethodInvocation(proxy, this.adviceSupport.getTarget(), method, args, this.adviceSupport.getTargetClass(), interceptorsAndDynamicMethodMatchers);
        return invocation.proceed();
    }
}
