package com.cxh.spring.demo.controller;

import com.cxh.spring.demo.service.DemoService;
import com.cxh.spring.mvcframework.annotation.BlseAutowired;
import com.cxh.spring.mvcframework.annotation.BlseController;
import com.cxh.spring.mvcframework.annotation.BlseRequestMapping;

/**
 * @author cxh
 * @date: 2021/1/7
 * @description:
 */

@BlseController
@BlseRequestMapping("/demo")
public class DemoController {

    @BlseAutowired
    private DemoService demoService;

    @BlseRequestMapping("/")
    public String demo() {
        return "successsss";
    }

}
