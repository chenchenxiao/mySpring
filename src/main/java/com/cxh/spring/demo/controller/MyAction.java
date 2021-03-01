package com.cxh.spring.demo.controller;

;

import com.cxh.spring.demo.service.IModifyService;
import com.cxh.spring.demo.service.IQueryService;
import com.cxh.spring.framework.annotation.BlseAutowired;
import com.cxh.spring.framework.annotation.BlseController;
import com.cxh.spring.framework.annotation.BlseRequestMapping;
import com.cxh.spring.framework.annotation.BlseRequestParam;
import com.cxh.spring.framework.webmvc.BlseModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 公布接口url
 * @author Tom
 *
 */
@BlseController
@BlseRequestMapping("/web")
public class MyAction {

	@BlseAutowired
	IQueryService queryService;
	@BlseAutowired
	IModifyService modifyService;

	@BlseRequestMapping("/query.json")
	public BlseModelAndView query(HttpServletRequest request, HttpServletResponse response,
								  @BlseRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
    public void test() throws InvocationTargetException, IllegalAccessException {
        //获取处理器
        BlseHandlerAdapter ha = getHandlerAdapter(null);
        //调用方法，得到返回值
        BlseModelAndView mv = ha.handle(null, null, null);
    }

    public void test1() throws InvocationTargetException, IllegalAccessException {
        //获取处理器
        BlseHandlerAdapter ha = getHandlerAdapter(null);
        //调用方法，得到返回值
        BlseModelAndView mv = ha.handle(null, null, null);
    }

    public void test2() throws InvocationTargetException, IllegalAccessException {
        //获取处理器
        BlseHandlerAdapter ha = getHandlerAdapter(null);
        //调用方法，得到返回值
        BlseModelAndView mv = ha.handle(null, null, null);
    }

    public void test3() throws InvocationTargetException, IllegalAccessException {
        //获取处理器
        BlseHandlerAdapter ha = getHandlerAdapter(null);
        //调用方法，得到返回值
        BlseModelAndView mv = ha.handle(null, null, null);
    }
	
	@BlseRequestMapping("/add*.json")
	public BlseModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @BlseRequestParam("name") String name,@BlseRequestParam("addr") String addr){
		String result = null;
		try {
			result = modifyService.add(name,addr);
			return out(response,result);
		} catch (Exception e) {
//			e.printStackTrace();
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("detail",e.getMessage());
//			System.out.println(Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
			return new BlseModelAndView("500",model);
		}

	}
	
	@BlseRequestMapping("/remove.json")
	public BlseModelAndView remove(HttpServletRequest request,HttpServletResponse response,
		   @BlseRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@BlseRequestMapping("/edit.json")
	public BlseModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@BlseRequestParam("id") Integer id,
			@BlseRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private BlseModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
