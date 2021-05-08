package com.shaodw.aop;

/**
 * @author shaodw
 * @date 2021/5/5 23:36
 * @description
 */
public class MyServiceImpl implements MyService{
	@Override
	public void myMethod() {
		System.out.println("myserviceimpl myMethod invoked! ");
	}
}
