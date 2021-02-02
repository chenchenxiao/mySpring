package com.cxh.spring.framework.config;

import lombok.Data;

/**
 * @author cxh
 * @date: 2021/1/19
 * @description:
 * 用来存储配置文件中的信息，相当于保存在内存中的配置
 */

@Data
public class BlseBeanDefinition {

    private String beanName;

    private String factoryBeanName;

    private String beanClassName;

    private boolean lazyInit = false;

}
