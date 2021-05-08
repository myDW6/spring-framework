package com.shaodw.aop;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;

/**
 * @author shaodw
 * @date 2021/5/5 23:48
 * @description
 */
public class MyTest {
	public static void main(String[] args) {
		ClassPathResource classPathResource = new ClassPathResource("applicationContext.xml");
		DefaultListableBeanFactory defaultListableBeanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(defaultListableBeanFactory);
		xmlBeanDefinitionReader.loadBeanDefinitions(classPathResource);

		MyService aop = (MyService) defaultListableBeanFactory.getBean("myAop");//myAop对应的是ProxyFactoryBean 返回的不是
		//ProxyFactoryBean的实例哦 而是其工厂生产出来的bean
		aop.myMethod();//进入到与之关联的InvocationHandler之中 这里aop是一个代理对象$Proxy 它关联的InvocationHandler是父类Proxy关联的InvocationHandler  这里就是JDKDynamicAopProxy
		System.out.println(aop.getClass());//class com.sun.proxy.$Proxy4
		Arrays.stream(aop.getClass().getInterfaces()).forEach(System.out::println);
		Object bean = defaultListableBeanFactory.getBean("&myAop");
		System.out.println(bean);
		MyService aop1 = (MyService) defaultListableBeanFactory.getBean("myAop");//myAop对应的是ProxyFactoryBean 返回的不是
		System.out.println(aop == aop1);
	}
}
