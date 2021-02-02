package com.cxh.spring.demo.aspect;



import com.cxh.spring.framework.aop.aspect.BlseJoinPoint;


import java.util.Arrays;

/**
 * Created by Tom.
 */

public class LogAspect {

    //在调用一个方法之前，执行before方法
    public void before(BlseJoinPoint joinPoint){
        joinPoint.setUserAttribute("startTime_" + joinPoint.getMethod().getName(),System.currentTimeMillis());
        //这个方法中的逻辑，是由我们自己写的
        System.out.println("Invoker Before Method!!! TargetObject:" +  joinPoint.getThis() + "Args:" + Arrays.toString(joinPoint.getArguments()));
    }

    //在调用一个方法之后，执行after方法
    public void after(BlseJoinPoint joinPoint){
        System.out.println("Invoker After Method!!!" +
                "\nTargetObject:" +  joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()));
        long startTime = (Long) joinPoint.getUserAttribute("startTime_" + joinPoint.getMethod().getName());
        long endTime = System.currentTimeMillis();
        System.out.println("use time :" + (endTime - startTime));
    }

    public void afterThrowing(BlseJoinPoint joinPoint, Throwable ex){
        System.out.println("出现异常" +
                "\nTargetObject:" +  joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()) +
                "\nThrows:" + ex.getMessage());
    }

}
