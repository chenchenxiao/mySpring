package com.cxh.spring.framework.aop.config;

import lombok.Data;

/**
 * @author cxh
 * @date: 2021/1/24
 * @description:
 */

@Data
public class BlseAopConfig {

    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowName;

}
