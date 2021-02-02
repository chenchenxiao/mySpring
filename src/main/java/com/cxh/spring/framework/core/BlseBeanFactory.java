package com.cxh.spring.framework.core;

/**
 * @author cxh
 * @date: 2021/1/19
 * @description:
 */
public interface BlseBeanFactory {
    
    Object getBean(String beanBame);

    Object getBean(Class clazz);

}
