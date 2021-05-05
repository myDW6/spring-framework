package com.shaodw.helloworld;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author shaodw
 * @date 2021/5/5 16:23
 * @description 测试Spring源码环境是否搭建成功
 */

public class HelloWorldTest {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ioc = new AnnotationConfigApplicationContext(Config.class);
		ioc.getBean(Hello.class).hello();
	}
}
