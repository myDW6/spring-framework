/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.env;

/**
 * Interface indicating a component that contains and exposes an {@link Environment} reference.
 *
 * <p>All Spring application contexts are EnvironmentCapable, and the interface is used primarily
 * for performing {@code instanceof} checks in framework methods that accept BeanFactory
 * instances that may or may not actually be ApplicationContext instances in order to interact
 * with the environment if indeed it is available.
 *
 * 它是具有获取并公开 Environment 引用的接口。
 *
 * 所有 Spring 的 ApplicationContext 都具有 EnvironmentCapable 功能，并且该接口主要用于在接受 BeanFactory 实例的框架方法中执行 instanceof 检查，
 * 以便可以与环境进行交互（如果实际上是 ApplicationContext 实例）。
 * Environment 是 SpringFramework 中抽象出来的类似于运行环境的独立抽象，它内部存放着应用程序运行的一些配置。
 *
 * <p>As mentioned, {@link org.springframework.context.ApplicationContext ApplicationContext}
 * extends EnvironmentCapable, and thus exposes a {@link #getEnvironment()} method; however,
 * {@link org.springframework.context.ConfigurableApplicationContext ConfigurableApplicationContext}
 * redefines {@link org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * getEnvironment()} and narrows the signature to return a {@link ConfigurableEnvironment}.
 * The effect is that an Environment object is 'read-only' until it is being accessed from
 * a ConfigurableApplicationContext, at which point it too may be configured.
 *上面所述，ApplicationContext 扩展了 EnvironmentCapable ，因此公开了 getEnvironment() 方法
 * 但是，ConfigurableApplicationContext 重新定义了 getEnvironment() 并缩小了签名范围，
 * 以返回 ConfigurableEnvironment 。结果是环境对象是 “只读的” ，
 * 直到从 ConfigurableApplicationContext 访问它为止，此时也可以对其进行配置。
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see ConfigurableEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment()
 */
//在 SpringFramework 中，以 Capable 结尾的接口，通常意味着可以通过这个接口的某个特定的方法（通常是 getXXX() ）拿到特定的组件
public interface EnvironmentCapable {

	/**
	 * Return the {@link Environment} associated with this component.
	 */
	Environment getEnvironment();

}
