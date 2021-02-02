package com.cxh.spring.framework.aop.support;


import com.cxh.spring.framework.aop.BlseApoProxy;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */
public class BlseCglibApoProxy implements BlseApoProxy {

    private BlseAdviceSupport config;

    public BlseCglibApoProxy(BlseAdviceSupport config) {
        this.config = config;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
