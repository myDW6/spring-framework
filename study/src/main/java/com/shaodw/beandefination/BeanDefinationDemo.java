package com.shaodw.beandefination;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * @author shaodw
 * @date 2021/5/17 22:30
 * @description
 */
public class BeanDefinationDemo {

	public static void main(String[] args) {
		//通过BeanDefinitionBuilder构建
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(User.class);//一般性的BeanDefinition 非根
//		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(User.class); //root BeanDefinition
		//通过属性设置
		builder.addPropertyValue("name", "shadw").addPropertyValue("age", 11);
		//获取BeanDefinition实例
		BeanDefinition beanDefinition = builder.getBeanDefinition();
		//beandefination 并非bean终态 可以任意修改

		//使用派生类
		GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
		genericBeanDefinition.setBeanClass(User.class);
		MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
		mutablePropertyValues.addPropertyValue("age", 1);
		mutablePropertyValues.addPropertyValue("name", "shdadw");
		//也可以这样
		mutablePropertyValues.add("name", "shaodw").add("age", 11);
		genericBeanDefinition.setPropertyValues(mutablePropertyValues);


	}
}
