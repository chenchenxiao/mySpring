package com.cxh.spring.framework.aop;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */

//默认使用JDK代理
public interface BlseApoProxy {

    Object getProxy();
    Object getProxy(ClassLoader classLoader);

}
