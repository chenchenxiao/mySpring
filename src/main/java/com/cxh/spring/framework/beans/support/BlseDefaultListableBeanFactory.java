package com.cxh.spring.framework.beans.support;



import com.cxh.spring.framework.config.BlseBeanDefinition;
import com.cxh.spring.framework.context.support.BlseAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cxh
 * @date: 2021/1/19
 * @description:
 */
public class BlseDefaultListableBeanFactory extends BlseAbstractApplicationContext {

    protected final Map<String, BlseBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,BlseBeanDefinition>();

}
