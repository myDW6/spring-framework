package com.shaodw.helloworld;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shaodw
 * @date 2021/5/16 23:57
 * @description
 */
@Configuration
public class AnnotaionDemo {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(AnnotaionDemo.class);
		applicationContext.refresh();
	}

	@Bean
	public User user(){
		//将java代码变成配置
		User user = new User();
		user.setName("shadow");
		user.setAge(10);
		return user;
	}
}

class User{
	private String name;
	private int age;

	public void setName(String name) {
		this.name = name;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
