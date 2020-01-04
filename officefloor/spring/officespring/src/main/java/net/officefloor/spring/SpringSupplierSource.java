/*-
 * #%L
 * Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.spring;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.FatalBeanException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.impl.structure.SupplierThreadLocalNodeImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.plugin.section.clazz.Property;
import net.officefloor.spring.extension.SpringBeanDecoratorContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionContext;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Spring {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringSupplierSource extends AbstractSupplierSource {

	/**
	 * <p>
	 * Obtains the bean from {@link OfficeFloor}.
	 * <p>
	 * This should be used as follows:
	 * 
	 * <pre>
	 * &#64;Configuration
	 * public class SomeConfigurationOnScanPath {
	 * 
	 * 	&#64;Bean
	 * 	public DependencyType officeFloorDependency() {
	 * 		return SpringSupplierSource.getManagedObject("qualifier", DependencyType.class);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param            <O> Object type.
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Type of object required.
	 * @return Object sourced from an {@link OfficeFloor} {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public static <O> O getManagedObject(String qualifier, Class<? extends O> objectType) {

		// Obtain the dependency factory
		SpringDependencyFactory factory = springDependencyFactory.get();
		if (factory == null) {
			throw new IllegalStateException("Attempting to create " + OfficeFloor.class.getSimpleName()
					+ " dependency for Spring outside setup.  Ensure dependency in Spring is configured to be used as singleton.");
		}

		// Create and return the dependency
		try {
			return (O) factory.createDependency(qualifier, objectType);

		} catch (Throwable ex) {
			// Propagate as fatal error
			throw new FatalBeanException("Failed to obtain " + OfficeFloor.class.getSimpleName() + " dependency "
					+ SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, objectType.getName()));
		}
	}

	/**
	 * Access to {@link SpringDependencyFactory} to create the {@link OfficeFloor}
	 * dependencies for Spring.
	 */
	private static final ThreadLocal<SpringDependencyFactory> springDependencyFactory = new ThreadLocal<>();

	/**
	 * <p>
	 * Allows capturing the {@link ConfigurableApplicationContext}.
	 * <p>
	 * Typically this is for testing to enable using Spring beans.
	 * 
	 * @param capture {@link Consumer} to receive the
	 *                {@link ConfigurableApplicationContext}.
	 * @param loader  {@link SpringLoader}.
	 * @return Loaded context.
	 * @throws E Possible failure in loading.
	 */
	public static <S, E extends Throwable> S captureApplicationContext(Consumer<ConfigurableApplicationContext> capture,
			SpringLoader<S, E> loader) throws E {

		// Provide capture
		applicationContextCapture.set(capture);
		try {

			// Undertake the load
			return loader.load();

		} finally {
			// Ensure clear capture (no longer loading)
			applicationContextCapture.set(null);
		}
	}

	/**
	 * {@link Consumer} to receive the {@link ConfigurableApplicationContext}.
	 */
	private static final ThreadLocal<Consumer<ConfigurableApplicationContext>> applicationContextCapture = new ThreadLocal<>();

	/**
	 * Provides the loading of Spring.
	 * 
	 * @param <S> Spring loaded object to return.
	 * @param <E> Possible failure in loading.
	 */
	public static interface SpringLoader<S, E extends Throwable> {

		/**
		 * Loads the Spring item.
		 * 
		 * @return Spring item.
		 * @throws E Possible failure in loading.
		 */
		S load() throws E;
	}

	/**
	 * Factory for the creation of the Spring dependencies.
	 */
	public static interface SpringDependencyFactory {

		/**
		 * Creates the dependency.
		 * 
		 * @param qualifier  Qualifier. May be <code>null</code>.
		 * @param objectType Object type required.
		 * @return Dependency object.
		 * @throws Exception Possible {@link Exception} in creating the dependency.
		 */
		Object createDependency(String qualifier, Class<?> objectType) throws Exception;
	}

	/**
	 * Runs the {@link Runnable} in context for the {@link SpringDependencyFactory}
	 * to create additional beans for Spring.
	 * 
	 * @param         <S> Loaded context.
	 * @param         <E> Possible {@link Throwable} from loading.
	 * @param loader  {@link SpringLoader}.
	 * @param factory {@link SpringDependencyFactory} to create the additional
	 *                beans.
	 * @return Loaded context.
	 * @throws E If fails to load.
	 */
	public static <S, E extends Throwable> S runInContext(SpringLoader<S, E> loader, SpringDependencyFactory factory)
			throws E {

		// Provide factory for creating OfficeFloor dependencies
		springDependencyFactory.set(factory);
		try {

			// Undertake the load
			return loader.load();

		} finally {
			// Ensure clear factory (no longer loading)
			springDependencyFactory.set(null);
		}
	}

	/**
	 * Convenience method for configuring Spring programmatically into an
	 * {@link Office}.
	 * 
	 * @param architect                        {@link OfficeArchitect}.
	 * @param configurationClass               Spring Boot configuration
	 *                                         {@link Class}.
	 * @param additionalPropertyNameValuePairs Additional {@link Property}
	 *                                         name/value pairs.
	 * @return {@link OfficeSupplier} for the {@link SpringSupplierSource}.
	 */
	public static OfficeSupplier configure(OfficeArchitect architect, Class<?> configurationClass,
			String... additionalPropertyNameValuePairs) {
		OfficeSupplier supplier = architect.addSupplier("Spring", SpringSupplierSource.class.getName());
		supplier.addProperty(SpringSupplierSource.CONFIGURATION_CLASS_NAME, configurationClass.getName());
		for (int i = 0; i < additionalPropertyNameValuePairs.length; i += 2) {
			String name = additionalPropertyNameValuePairs[i];
			String value = additionalPropertyNameValuePairs[i + 1];
			supplier.addProperty(name, value);
		}
		return supplier;
	}

	/**
	 * Name of {@link Property} for the Spring Boot configuration {@link Class}.
	 */
	public static final String CONFIGURATION_CLASS_NAME = "configuration.class";

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext springContext;

	/**
	 * Decorates the Spring bean.
	 * 
	 * @param beanName           Name of Spring Bean.
	 * @param beanType           Type of the Spring Bean.
	 * @param extensions         {@link SpringSupplierExtension} instances.
	 * @param springDependencies {@link SpringDependency} instances by their name.
	 * @return {@link SpringDependency} instances for the Spring Bean.
	 * @throws Exception If fails to decorate the Spring Bean.
	 */
	private SpringDependency[] decorateBean(String beanName, Class<?> beanType,
			List<SpringSupplierExtension> extensions, Map<String, SpringDependency> springDependencies)
			throws Exception {

		// Decorate the beans via the extensions
		SpringBeanDecoratorContextImpl decoratorContext = new SpringBeanDecoratorContextImpl(beanName, beanType,
				springDependencies);
		for (SpringSupplierExtension extension : extensions) {
			extension.decorateSpringBean(decoratorContext);
		}

		// Return the spring dependencies
		return decoratorContext.dependencies.values().stream().toArray(SpringDependency[]::new);
	}

	/*
	 * ================== SupplierSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CONFIGURATION_CLASS_NAME, "Configuration Class");
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Load the extensions
		List<SpringSupplierExtension> extensions = new LinkedList<>();
		for (SpringSupplierExtension extension : context
				.loadOptionalServices(SpringSupplierExtensionServiceFactory.class)) {
			extensions.add(extension);
		}

		// Create the dependency factory
		Map<String, Object> existingSpringDependencies = new HashMap<>();
		Map<String, SpringDependency> springDependencies = new HashMap<>();
		SpringDependencyFactory dependencyFactory = (qualifier, objectType) -> {

			// Obtain the dependency name
			String dependencyName = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier,
					objectType.getName());

			// Determine if already created
			Object dependency = existingSpringDependencies.get(dependencyName);
			if (dependency != null) {
				return dependency;
			}

			// Obtain the supplier thread local
			SupplierThreadLocal<?> threadLocal = context.addSupplierThreadLocal(qualifier, objectType);

			// Register the Spring dependency
			springDependencies.put(dependencyName, new SpringDependency(qualifier, objectType));

			// Create the dependency to supplier thread local
			dependency = Proxy.newProxyInstance(context.getClassLoader(), new Class[] { objectType },
					(proxy, method, args) -> {

						// Ensure obtain the object
						Object object = threadLocal.get();
						if (object == null) {
							throw new IllegalStateException(OfficeFloor.class.getSimpleName() + " supplied bean for "
									+ dependencyName + " is not available");
						}

						// Invoke the method on the object
						return object.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(object,
								args);
					});

			// Register the dependency and return it
			existingSpringDependencies.put(dependencyName, dependency);
			return dependency;
		};

		// Create the extension context
		SpringSupplierExtensionContext extensionContext = new SpringSupplierExtensionContext() {

			@Override
			@SuppressWarnings("unchecked")
			public <B> B getManagedObject(String qualifier, Class<? extends B> objectType) throws Exception {
				return (B) dependencyFactory.createDependency(qualifier, objectType);
			}

			@Override
			public void addThreadSynchroniser(ThreadSynchroniserFactory threadSynchroniserFactory) {
				context.addThreadSynchroniser(threadSynchroniserFactory);
			}
		};

		// Load Spring with access to hook in OfficeFloor managed objects
		this.springContext = runInContext(() -> {

			// Run before spring load
			for (SpringSupplierExtension extension : extensions) {
				extension.beforeSpringLoad(extensionContext);
			}

			// Load the configurable application context
			String configurationClassName = context.getProperty(CONFIGURATION_CLASS_NAME);
			Class<?> configurationClass = context.loadClass(configurationClassName);
			ConfigurableApplicationContext applicationContext = SpringApplication.run(configurationClass);

			// Run after spring load
			for (SpringSupplierExtension extension : extensions) {
				extension.afterSpringLoad(extensionContext);
			}

			// Return the application context
			return applicationContext;

		}, dependencyFactory);

		// Determine if capture the application context
		Consumer<ConfigurableApplicationContext> captureApplicationContext = applicationContextCapture.get();
		if (captureApplicationContext != null) {
			captureApplicationContext.accept(this.springContext);
		}

		// Load listing of all the beans (mapped by their type)
		Map<Class<?>, List<String>> beanNamesByType = new HashMap<>();
		NEXT_BEAN: for (String name : this.springContext.getBeanDefinitionNames()) {

			// Load the bean type
			Class<?> beanType = this.springContext.getBean(name).getClass();

			// Filter out Spring beans being loaded from OfficeFloor
			for (SpringDependency dependency : springDependencies.values()) {
				if (dependency.getObjectType().isAssignableFrom(beanType)) {
					continue NEXT_BEAN; // OfficeFloor providing
				}
			}

			// Add the bean
			List<String> beanNames = beanNamesByType.get(beanType);
			if (beanNames == null) {
				beanNames = new LinkedList<>();
				beanNamesByType.put(beanType, beanNames);
			}
			beanNames.add(name);
		}

		// Load the supplied managed object sources
		for (Class<?> beanType : beanNamesByType.keySet()) {

			List<String> beanNames = beanNamesByType.get(beanType);
			SpringDependency[] springDependenciesList;
			switch (beanNames.size()) {
			case 1:
				// Only the one type (so no qualifier)
				String singleBeanName = beanNames.get(0);

				// Decorate the bean
				springDependenciesList = this.decorateBean(singleBeanName, beanType, extensions, springDependencies);

				// Load the single (unqualified) bean
				context.addManagedObjectSource(null, beanType, new SpringBeanManagedObjectSource(singleBeanName,
						beanType, this.springContext, springDependenciesList));
				break;

			default:
				// Multiple, so provide qualifier
				for (String beanName : beanNames) {

					// Decorate the bean
					springDependenciesList = this.decorateBean(beanName, beanType, extensions, springDependencies);

					// Load the qualified bean
					context.addManagedObjectSource(beanName, beanType, new SpringBeanManagedObjectSource(beanName,
							beanType, this.springContext, springDependenciesList));
				}
				break;
			}
		}
	}

	@Override
	public void terminate() {

		// Close the spring context
		this.springContext.close();
	}

	/**
	 * {@link SpringBeanDecoratorContext} implementation.
	 */
	private static class SpringBeanDecoratorContextImpl implements SpringBeanDecoratorContext {

		/**
		 * Bean name.
		 */
		private final String name;

		/**
		 * Bean type.
		 */
		private final Class<?> type;

		/**
		 * {@link SpringDependency} instances by name.
		 */
		private final Map<String, SpringDependency> dependencies;

		/**
		 * Instantiate.
		 * 
		 * @param name         Bean name.
		 * @param type         Bean type.
		 * @param dependencies {@link SpringDependency} instances by name.
		 */
		public SpringBeanDecoratorContextImpl(String name, Class<?> type, Map<String, SpringDependency> dependencies) {
			this.name = name;
			this.type = type;
			this.dependencies = new HashMap<>(dependencies);
		}

		/*
		 * ====================== SpringBeanDecoratorContext ========================
		 */

		@Override
		public String getBeanName() {
			return this.name;
		}

		@Override
		public Class<?> getBeanType() {
			return this.type;
		}

		@Override
		public void addDependency(String qualifier, Class<?> type) {

			// Obtain the dependency name
			String dependencyName = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, type.getName());
			if (this.dependencies.containsKey(dependencyName)) {
				return; // already have dependency
			}

			// Add the dependency
			this.dependencies.put(dependencyName, new SpringDependency(qualifier, type));
		}
	}

}
