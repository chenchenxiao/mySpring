package com.cxh.spring.framework.aop.intercept;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public interface BlseMethodInterceptor {

    Object invoke(BlseMethodInvocation mi) throws Throwable;

}
