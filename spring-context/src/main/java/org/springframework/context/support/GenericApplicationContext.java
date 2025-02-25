/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.context.support;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Generic ApplicationContext implementation that holds a single internal
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
 * instance and does not assume a specific bean definition format. Implements
 * the {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
 * interface in order to allow for applying any bean definition readers to it.
 *
 * 通用 ApplicationContext 的实现，该实现拥有一个内部 DefaultListableBeanFactory 实例，并且不采用特定的 bean 定义格式。
 * 另外它实现 BeanDefinitionRegistry 接口，以便允许将任何 bean 定义读取器应用于该容器中。
 * GenericApplicationContext 和AbstractRefreshableApplicationContext都组合了BeanFactory ，也就是说目前ApplicationContext所有的实现类都组合了BeanFactory，
 * 由此可以得到一个非常非常重要的信息：ApplicationContext 并不是继承了 BeanFactory 的容器，而是组合了 BeanFactory ！ ApplicationContext同时也实现了BeanFactory接口
 *
 * <p>Typical usage is to register a variety of bean definitions via the
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
 * interface and then call {@link #refresh()} to initialize those beans
 * with application context semantics (handling
 * {@link org.springframework.context.ApplicationContextAware}, auto-detecting
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessors},
 * etc).
 * 典型的用法是通过 BeanDefinitionRegistry 接口注册各种 Bean 的定义
 * ，然后调用 refresh() 以使用应用程序上下文语义来初始化这些 Bean（处理 ApplicationContextAware ，自动检测 BeanFactoryPostProcessors 等）。
 *GenericApplicationContext借助BeanDefinitionRegistry处理特殊Bean
 *BeanDefinitionRegistry  Bean 定义的注册器，GenericApplicationContext 实现了它
 * ，可以自定义注册一些 Bean 。然而在 GenericApplicationContext 中，它实现的定义注册方法 registerBeanDefinition ，
 * 在底层还是调用的 DefaultListableBeanFactory 执行 registerBeanDefinition 方法，说明它也没有对此做什么扩展。
 *
 * <p>In contrast to other ApplicationContext implementations that create a new
 * internal BeanFactory instance for each refresh, the internal BeanFactory of
 * this context is available right from the start, to be able to register bean
 * definitions on it. {@link #refresh()} may only be called once.
 * 与为每次刷新创建一个新的内部 BeanFactory 实例的其他 ApplicationContext 实现(AbstractRefreshableApplicationContext)相反，
 * 此上下文的内部 BeanFactory 从一开始就可用，以便能够在其上注册 Bean 定义。 refresh() 只能被调用一次。
 * 由于 GenericApplicationContext 中组合了一个 DefaultListableBeanFactory ，
 * 而这个 BeanFactory 是在 GenericApplicationContext 的构造方法中就已经初始化好了，那么初始化好的 BeanFactory 就不允许在运行期间被重复刷新了
 * <p>Usage example:
 *
 * <pre class="code">
 * GenericApplicationContext ctx = new GenericApplicationContext();
 * XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
 * xmlReader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));
 * PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(ctx);
 * propReader.loadBeanDefinitions(new ClassPathResource("otherBeans.properties"));
 * ctx.refresh();
 *
 * MyBean myBean = (MyBean) ctx.getBean("myBean");
 * ...</pre>
 *
 * For the typical case of XML bean definitions, simply use
 * {@link ClassPathXmlApplicationContext} or {@link FileSystemXmlApplicationContext},
 * which are easier to set up - but less flexible, since you can just use standard
 * resource locations for XML bean definitions, rather than mixing arbitrary bean
 * definition formats. The equivalent in a web environment is
 * {@link org.springframework.web.context.support.XmlWebApplicationContext}.
 * 对于 XML Bean 定义的典型情况，只需使用 ClassPathXmlApplicationContext 或 FileSystemXmlApplicationContext ，
 * 因为它们更易于设置（但灵活性较差，因为只能将从标准的资源配置文件中读取 XML Bean 定义，而不能混合使用任意 Bean 定义的格式）。
 * 在 Web 环境中，替代方案是 XmlWebApplicationContext 。
 * 这段注释它提到了 xml 的配置，咱之前也讲过，注解驱动的 IOC 容器可以导入 xml 配置文件，
 * 不过如果大多数都是 xml 配置的话，官方建议还是直接用 ClassPathXmlApplicationContext 或者 FileSystemXmlApplicationContext 就好。
 * 对比起灵活度来讲，咱也能清晰地认识到：注解驱动的方式在开发时很灵活，但如果需要修改配置时，
 * 可能需要重新编译配置类；xml 驱动的方式在修改配置时直接修改即可，不需要做任何额外的操作，
 * 但能配置的内容实在是有些有限。所以这也建议咱开发者在实际开发中，要权衡对比着使用。
 *
 * <p>For custom application context implementations that are supposed to read
 * special bean definition formats in a refreshable manner, consider deriving
 * from the {@link AbstractRefreshableApplicationContext} base class.
 * 对于应该以可刷新方式读取特殊bean定义格式的自定义应用程序上下文实现，请考虑从 AbstractRefreshableApplicationContext 基类派生。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.2
 * @see #registerBeanDefinition
 * @see #refresh()
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
 */
//注解驱动的 IOC 容器
public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

	private final DefaultListableBeanFactory beanFactory;

	@Nullable
	private ResourceLoader resourceLoader;

	private boolean customClassLoader = false;

	private final AtomicBoolean refreshed = new AtomicBoolean();


	/**
	 * Create a new GenericApplicationContext.
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext() {
		// 内置的beanFactory在GenericApplicationContext创建时就已经初始化好了
		this.beanFactory = new DefaultListableBeanFactory();
	}

	/**
	 * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
	}

	/**
	 * Create a new GenericApplicationContext with the given parent.
	 * @param parent the parent application context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(@Nullable ApplicationContext parent) {
		this();
		setParent(parent);
	}

	/**
	 * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 * @param parent the parent application context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		this(beanFactory);
		setParent(parent);
	}


	/**
	 * Set the parent of this application context, also setting
	 * the parent of the internal BeanFactory accordingly.
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
	 */
	@Override
	public void setParent(@Nullable ApplicationContext parent) {
		super.setParent(parent);
		this.beanFactory.setParentBeanFactory(getInternalParentBeanFactory());
	}

	@Override
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		super.setApplicationStartup(applicationStartup);
		this.beanFactory.setApplicationStartup(applicationStartup);
	}

	/**
	 * Set whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. Default is "true".
	 * @since 3.0
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.beanFactory.setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding);
	}

	/**
	 * Set whether to allow circular references between beans - and automatically
	 * try to resolve them.
	 * <p>Default is "true". Turn this off to throw an exception when encountering
	 * a circular reference, disallowing them completely.
	 * @since 3.0
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 */
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.beanFactory.setAllowCircularReferences(allowCircularReferences);
	}

	/**
	 * Set a ResourceLoader to use for this context. If set, the context will
	 * delegate all {@code getResource} calls to the given ResourceLoader.
	 * If not set, default resource loading will apply.
	 * <p>The main reason to specify a custom ResourceLoader is to resolve
	 * resource paths (without URL prefix) in a specific fashion.
	 * The default behavior is to resolve such paths as class path locations.
	 * To resolve resource paths as file system locations, specify a
	 * FileSystemResourceLoader here.
	 * <p>You can also pass in a full ResourcePatternResolver, which will
	 * be autodetected by the context and used for {@code getResources}
	 * calls as well. Else, default resource pattern matching will apply.
	 * @see #getResource
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.core.io.FileSystemResourceLoader
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see #getResources
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}


	//---------------------------------------------------------------------
	// ResourceLoader / ResourcePatternResolver override if necessary
	//---------------------------------------------------------------------

	/**
	 * This implementation delegates to this context's ResourceLoader if set,
	 * falling back to the default superclass behavior else.
	 * @see #setResourceLoader
	 */
	@Override
	public Resource getResource(String location) {
		if (this.resourceLoader != null) {
			return this.resourceLoader.getResource(location);
		}
		return super.getResource(location);
	}

	/**
	 * This implementation delegates to this context's ResourceLoader if it
	 * implements the ResourcePatternResolver interface, falling back to the
	 * default superclass behavior else.
	 * @see #setResourceLoader
	 */
	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		if (this.resourceLoader instanceof ResourcePatternResolver) {
			return ((ResourcePatternResolver) this.resourceLoader).getResources(locationPattern);
		}
		return super.getResources(locationPattern);
	}

	@Override
	public void setClassLoader(@Nullable ClassLoader classLoader) {
		super.setClassLoader(classLoader);
		this.customClassLoader = true;
	}

	@Override
	@Nullable
	public ClassLoader getClassLoader() {
		if (this.resourceLoader != null && !this.customClassLoader) {
			return this.resourceLoader.getClassLoader();
		}
		return super.getClassLoader();
	}


	//---------------------------------------------------------------------
	// Implementations of AbstractApplicationContext's template methods
	//---------------------------------------------------------------------

	/**
	 * Do nothing: We hold a single internal BeanFactory and rely on callers
	 * to register beans through our public methods (or the BeanFactory's).
	 * @see #registerBeanDefinition
	 */
	@Override
	protected final void refreshBeanFactory() throws IllegalStateException {
		//// 利用CAS，保证只能设置一次true，如果出现第二次，就抛出重复刷新异常
		if (!this.refreshed.compareAndSet(false, true)) {
			throw new IllegalStateException(
					"GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
		}
		this.beanFactory.setSerializationId(getId());
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		this.beanFactory.setSerializationId(null);
		super.cancelRefresh(ex);
	}

	/**
	 * Not much to do: We hold a single internal BeanFactory that will never
	 * get released.
	 */
	@Override
	protected final void closeBeanFactory() {
		this.beanFactory.setSerializationId(null);
	}

	/**
	 * Return the single internal BeanFactory held by this context
	 * (as ConfigurableListableBeanFactory).
	 */
	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Return the underlying bean factory of this context,
	 * available for registering bean definitions.
	 * <p><b>NOTE:</b> You need to call {@link #refresh()} to initialize the
	 * bean factory and its contained beans with application context semantics
	 * (autodetecting BeanFactoryPostProcessors, etc).
	 * @return the internal bean factory (as DefaultListableBeanFactory)
	 */
	public final DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return this.beanFactory;
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		assertBeanFactoryActive();
		return this.beanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry
	//---------------------------------------------------------------------

	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		this.beanFactory.removeBeanDefinition(beanName);
	}

	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getBeanDefinition(beanName);
	}

	@Override
	public boolean isBeanNameInUse(String beanName) {
		return this.beanFactory.isBeanNameInUse(beanName);
	}

	@Override
	public void registerAlias(String beanName, String alias) {
		this.beanFactory.registerAlias(beanName, alias);
	}

	@Override
	public void removeAlias(String alias) {
		this.beanFactory.removeAlias(alias);
	}

	@Override
	public boolean isAlias(String beanName) {
		return this.beanFactory.isAlias(beanName);
	}


	//---------------------------------------------------------------------
	// Convenient methods for registering individual beans
	//---------------------------------------------------------------------

	/**
	 * Register a bean from the given bean class, optionally providing explicit
	 * constructor arguments for consideration in the autowiring process.
	 * @param beanClass the class of the bean
	 * @param constructorArgs custom argument values to be fed into Spring's
	 * constructor resolution algorithm, resolving either all arguments or just
	 * specific ones, with the rest to be resolved through regular autowiring
	 * (may be {@code null} or empty)
	 * @since 5.2 (since 5.0 on the AnnotationConfigApplicationContext subclass)
	 */
	public <T> void registerBean(Class<T> beanClass, Object... constructorArgs) {
		registerBean(null, beanClass, constructorArgs);
	}

	/**
	 * Register a bean from the given bean class, optionally providing explicit
	 * constructor arguments for consideration in the autowiring process.
	 * @param beanName the name of the bean (may be {@code null})
	 * @param beanClass the class of the bean
	 * @param constructorArgs custom argument values to be fed into Spring's
	 * constructor resolution algorithm, resolving either all arguments or just
	 * specific ones, with the rest to be resolved through regular autowiring
	 * (may be {@code null} or empty)
	 * @since 5.2 (since 5.0 on the AnnotationConfigApplicationContext subclass)
	 */
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass, Object... constructorArgs) {
		registerBean(beanName, beanClass, (Supplier<T>) null,
				bd -> {
					for (Object arg : constructorArgs) {
						bd.getConstructorArgumentValues().addGenericArgumentValue(arg);
					}
				});
	}

	/**
	 * Register a bean from the given bean class, optionally customizing its
	 * bean definition metadata (typically declared as a lambda expression).
	 * @param beanClass the class of the bean (resolving a public constructor
	 * to be autowired, possibly simply the default constructor)
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 * @see #registerBean(String, Class, Supplier, BeanDefinitionCustomizer...)
	 */
	public final <T> void registerBean(Class<T> beanClass, BeanDefinitionCustomizer... customizers) {
		registerBean(null, beanClass, null, customizers);
	}

	/**
	 * Register a bean from the given bean class, optionally customizing its
	 * bean definition metadata (typically declared as a lambda expression).
	 * @param beanName the name of the bean (may be {@code null})
	 * @param beanClass the class of the bean (resolving a public constructor
	 * to be autowired, possibly simply the default constructor)
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 * @see #registerBean(String, Class, Supplier, BeanDefinitionCustomizer...)
	 */
	public final <T> void registerBean(
			@Nullable String beanName, Class<T> beanClass, BeanDefinitionCustomizer... customizers) {

		registerBean(beanName, beanClass, null, customizers);
	}

	/**
	 * Register a bean from the given bean class, using the given supplier for
	 * obtaining a new instance (typically declared as a lambda expression or
	 * method reference), optionally customizing its bean definition metadata
	 * (again typically declared as a lambda expression).
	 * @param beanClass the class of the bean
	 * @param supplier a callback for creating an instance of the bean
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 * @see #registerBean(String, Class, Supplier, BeanDefinitionCustomizer...)
	 */
	public final <T> void registerBean(
			Class<T> beanClass, Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		registerBean(null, beanClass, supplier, customizers);
	}

	/**
	 * Register a bean from the given bean class, using the given supplier for
	 * obtaining a new instance (typically declared as a lambda expression or
	 * method reference), optionally customizing its bean definition metadata
	 * (again typically declared as a lambda expression).
	 * <p>This method can be overridden to adapt the registration mechanism for
	 * all {@code registerBean} methods (since they all delegate to this one).
	 * @param beanName the name of the bean (may be {@code null})
	 * @param beanClass the class of the bean
	 * @param supplier a callback for creating an instance of the bean (in case
	 * of {@code null}, resolving a public constructor to be autowired instead)
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 */
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
			@Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		ClassDerivedBeanDefinition beanDefinition = new ClassDerivedBeanDefinition(beanClass);
		if (supplier != null) {
			beanDefinition.setInstanceSupplier(supplier);
		}
		for (BeanDefinitionCustomizer customizer : customizers) {
			customizer.customize(beanDefinition);
		}

		String nameToUse = (beanName != null ? beanName : beanClass.getName());
		registerBeanDefinition(nameToUse, beanDefinition);
	}


	/**
	 * {@link RootBeanDefinition} marker subclass for {@code #registerBean} based
	 * registrations with flexible autowiring for public constructors.
	 */
	@SuppressWarnings("serial")
	private static class ClassDerivedBeanDefinition extends RootBeanDefinition {

		public ClassDerivedBeanDefinition(Class<?> beanClass) {
			super(beanClass);
		}

		public ClassDerivedBeanDefinition(ClassDerivedBeanDefinition original) {
			super(original);
		}

		@Override
		@Nullable
		public Constructor<?>[] getPreferredConstructors() {
			Class<?> clazz = getBeanClass();
			Constructor<?> primaryCtor = BeanUtils.findPrimaryConstructor(clazz);
			if (primaryCtor != null) {
				return new Constructor<?>[] {primaryCtor};
			}
			Constructor<?>[] publicCtors = clazz.getConstructors();
			if (publicCtors.length > 0) {
				return publicCtors;
			}
			return null;
		}

		@Override
		public RootBeanDefinition cloneBeanDefinition() {
			return new ClassDerivedBeanDefinition(this);
		}
	}

}
