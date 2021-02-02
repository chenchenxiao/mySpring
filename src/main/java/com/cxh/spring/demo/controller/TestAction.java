package com.cxh.spring.demo.controller;




import com.cxh.spring.mvcframework.annotation.BlseController;
import com.cxh.spring.mvcframework.annotation.BlseRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@BlseController
@BlseRequestMapping("/mytest")
public class TestAction {

    @BlseRequestMapping("/")
    public void test(HttpServletRequest req, HttpServletResponse resp) {

        try {
            resp.getWriter().write("success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
