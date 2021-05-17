package com.shaodw.beandefination;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * @author shaodw
 * @date 2021/5/17 23:23
 * @description
 */
public class AnnotationBeanDefintaionDemo {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(Config.class);
		System.out.println(applicationContext.getBean("shaodw", User.class));
		System.out.println(applicationContext.getBean("shaodw1", User.class));
		System.out.println(applicationContext.getBean("shaodw2", User.class));
		applicationContext.close();
	}

	public static class Config{
		@Bean({"shaodw", "shaodw1", "shaodw2"})
		public User user(){
			User user = new User();
			user.setName("shaodw");
			user.setAge(11);
			return user;
		}
	}

	static void registerBeanDef(BeanDefinitionRegistry registry, String beanName, Class<?> c){
		AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(c).addPropertyValue("name", "shaodw")
				.addPropertyValue("age", 11)
				.getBeanDefinition();
		if (StringUtils.hasText(beanName)){
			//命名
			registry.registerBeanDefinition(beanName, beanDefinition);
		}else {
			//非命名
			BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
		}
	}
}
