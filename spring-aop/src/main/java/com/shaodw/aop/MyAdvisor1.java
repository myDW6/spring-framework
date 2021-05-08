package com.shaodw.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author shaodw
 * @date 2021/5/5 23:36
 * @description MethodInterceptor接口定义在aop联盟中
 */
public class MyAdvisor1 implements MethodInterceptor {
	@Nullable
	@Override
	//MethodInvocation封装了被代理的对象特定方法的引用
	public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
		System.out.println("这是advisor1 before myAdvisor invoked ");
		Object proceed = invocation.proceed();
		System.out.println("这是advisor1 after myAdvisor invoked ");
		return proceed;
	}
}
