package com.shaodw.main1;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;

/**
 * @author shaodw
 * @date 2021/5/5 16:46
 * @description
 */
public class BasicSpringProcess {
	public static void main(String[] args) {
		ClassPathResource classPathResource = new ClassPathResource("applicationContext.xml");
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
		xmlBeanDefinitionReader.loadBeanDefinitions(classPathResource);
		Student bean = (Student) beanFactory.getBean("stu");
		Student bean1 = (Student) beanFactory.getBean("stu");

		System.out.println(bean);

	}
}
