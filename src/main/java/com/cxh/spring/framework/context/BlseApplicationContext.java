package com.cxh.spring.framework.context;



import com.cxh.spring.framework.annotation.BlseAutowired;
import com.cxh.spring.framework.annotation.BlseController;
import com.cxh.spring.framework.annotation.BlseService;
import com.cxh.spring.framework.aop.config.BlseAopConfig;
import com.cxh.spring.framework.aop.support.BlseAdviceSupport;
import com.cxh.spring.framework.aop.support.BlseCglibApoProxy;
import com.cxh.spring.framework.aop.support.BlseJdkDynamicAopProxy;
import com.cxh.spring.framework.beans.BlseBeanWrapper;
import com.cxh.spring.framework.beans.support.BlseDefaultListableBeanFactory;
import com.cxh.spring.framework.config.BlseBeanDefinition;
import com.cxh.spring.framework.config.BlseBeanPostProcessor;
import com.cxh.spring.framework.context.support.BlseBeanDefinitionReader;
import com.cxh.spring.framework.core.BlseBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cxh
 * @date: 2021/1/19
 * @description:
 */
public class BlseApplicationContext extends BlseDefaultListableBeanFactory implements BlseBeanFactory {

    private String[] configurations;

    private BlseBeanDefinitionReader reader;

    //单例的IOC缓存
    private Map<String, Object> singletonBeanCacheMap = new ConcurrentHashMap<String, Object>();
    //通用的IOC容器
    private Map<String, BlseBeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, BlseBeanWrapper>();

    public BlseApplicationContext(String... configurations) {
        this.configurations = configurations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() throws Exception {
        //1. 定位配置文件
        reader = new BlseBeanDefinitionReader(this.configurations);
        //2. 加载配置文件，扫描相关的类，封装成beanDefinition
        List<BlseBeanDefinition> beanDefinitions = reader.loadBeanDefinition();
        //3. 注册，把配置信息放到容器里面
        doRegisterBeanDefinition(beanDefinitions);
        //4. 初始化非懒加载类
        doAutowrited();
    }

    private void doAutowrited() {
        for (Map.Entry<String, BlseBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doRegisterBeanDefinition(List<BlseBeanDefinition> beanDefinitions) throws Exception {
        for (BlseBeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception(beanDefinition.getFactoryBeanName() + " is exists");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    @Override
    public Object getBean(String beanName) {
        BlseBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        try {
            //生成通知事件
            BlseBeanPostProcessor beanPostProcessor = new BlseBeanPostProcessor();
            Object instance = instantiateBean(beanDefinition);
            if (instance == null) {
                return null;
            }
            //在实例初始化前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance,beanName);

            BlseBeanWrapper beanWrapper = new BlseBeanWrapper(instance);
            this.beanWrapperMap.put(beanName, beanWrapper);
            //实例初始化后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            populateBean(beanName, instance);
            //这样的调用留下了可操作控件
            return this.beanWrapperMap.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private void populateBean(String beanName, Object instance) {
        Class clazz = instance.getClass();
        if (!(clazz.isAnnotationPresent(BlseController.class) || clazz.isAnnotationPresent(BlseService.class))) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(BlseAutowired.class)) {
                continue;
            }
            BlseAutowired autowired = field.getAnnotation(BlseAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);
            if (null == this.beanWrapperMap.get(autowiredBeanName)) {
                continue;
            }
            try {
                field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //传一个BeanDefinition，返回一个实例bean
    private Object instantiateBean(BlseBeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            //根据class才能确定一个类是否有实例
            if (this.singletonBeanCacheMap.containsKey(className)) {
                instance = this.singletonBeanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                BlseAdviceSupport config = instantionAopConfig(beanDefinition);
                config.setTarget(instance);
                config.setTargetClass(clazz);
                if (config.pointCutMatch()) {
                    instance = createProxy(config);
                }
                this.singletonBeanCacheMap.put(beanDefinition.getFactoryBeanName(), instance);
                this.singletonBeanCacheMap.put(className, instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object createProxy(BlseAdviceSupport config) {
        Class clazz = config.getTargetClass();
        if (clazz.getInterfaces().length > 0) {
            Object proxy = new BlseJdkDynamicAopProxy(config).getProxy();
            return proxy;
        }
        return new BlseCglibApoProxy(config);

    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new  String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }

    @Override
    public Object getBean(Class clazz) {
        return getBean(clazz.getSimpleName());
    }

    private BlseAdviceSupport instantionAopConfig(BlseBeanDefinition beanDefinition) throws Exception{
        BlseAopConfig config = new BlseAopConfig();
        config.setPointCut(reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowName(reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new BlseAdviceSupport(config);
    }
}
