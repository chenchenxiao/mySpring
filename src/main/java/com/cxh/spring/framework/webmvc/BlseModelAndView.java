package com.cxh.spring.framework.webmvc;

import lombok.Data;

import java.util.Map;

/**
 * @author cxh
 * @date: 2021/1/23
 * @description:
 */

@Data
public class BlseModelAndView {

    private String viewName;

    private Map<String, ?> model;

    public BlseModelAndView(String viewName) {
        this(viewName, null);
    }

    public BlseModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }
}
