/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.context.event;

import org.springframework.context.ApplicationContext;

/**
 * Event raised when an {@code ApplicationContext} gets started.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see ContextStoppedEvent
 */
@SuppressWarnings("serial")
//ContextRefreshedEvent 事件的触发是所有单实例 Bean 刚创建完成后，就发布的事件，此时那些实现了 Lifecycle 接口的 Bean 还没有被回调 start 方法。当这些 start 方法被调用后，ContextStartedEvent 才会被触发。
public class ContextStartedEvent extends ApplicationContextEvent {

	/**
	 * Create a new ContextStartedEvent.
	 * @param source the {@code ApplicationContext} that has been started
	 * (must not be {@code null})
	 */
	public ContextStartedEvent(ApplicationContext source) {
		super(source);
	}

}
