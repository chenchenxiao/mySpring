package com.cxh.spring.framework.beans;

/**
 * @author cxh
 * @date: 2021/1/19
 * @description:
 */
public class BlseBeanWrapper {

    private Object wrappedInstance;

    private Class<?> wrappedClass;

    public BlseBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedClass.getClass();
    }
}
