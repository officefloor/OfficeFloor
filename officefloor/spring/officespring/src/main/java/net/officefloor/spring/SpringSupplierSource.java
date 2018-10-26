/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.spring;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.impl.structure.SupplierThreadLocalNodeImpl;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.plugin.section.clazz.Property;

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
	 * 		return SpringSupplierSource.getBean("qualifier", DependencyType.class);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param beanType  Type of bean required.
	 * @return Bean sourced from an {@link OfficeFloor} {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public static <B> B getBean(String qualifier, Class<? extends B> beanType) {

		// Obtain the dependency factory
		SpringDependencyFactory factory = springDependencyFactory.get();
		if (factory == null) {
			throw new IllegalStateException("Attempting to create " + OfficeFloor.class.getSimpleName()
					+ " dependency for Spring outside setup.  Ensure dependency in Spring is configured to be used as singleton.");
		}

		// Create and return the dependency
		try {
			return (B) factory.createDependency(qualifier, beanType);

		} catch (Throwable ex) {
			// Propagate as fatal error
			throw new FatalBeanException("Failed to obtain " + OfficeFloor.class.getSimpleName() + " dependency "
					+ SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, beanType.getName()));
		}
	}

	/**
	 * Access to {@link SpringDependencyFactory} to create the {@link OfficeFloor}
	 * dependencies for Spring.
	 */
	private static final ThreadLocal<SpringDependencyFactory> springDependencyFactory = new ThreadLocal<>();

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
	 * @param loader  {@link SpringLoader}.
	 * @param factory {@link SpringDependencyFactory} to create the additional
	 *                beans.
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
	 * Name of {@link Property} for the Spring Boot configuration {@link Class}.
	 */
	public static final String CONFIGURATION_CLASS_NAME = "configuration.class";

	/*
	 * ================== SupplierSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CONFIGURATION_CLASS_NAME, "Configuration Class");
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Load Spring with access to hook in OfficeFloor managed objects
		Map<String, SpringDependency> springDependenciesByName = new HashMap<>();
		ConfigurableApplicationContext springContext = runInContext(() -> {

			// Load the configurable application context
			String configurationClassName = context.getProperty(CONFIGURATION_CLASS_NAME);
			Class<?> configurationClass = context.loadClass(configurationClassName);
			return SpringApplication.run(configurationClass);

		}, (qualifier, objectType) -> {

			// Obtain the dependency name
			String dependencyName = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier,
					objectType.getName());

			// Obtain the supplier thread local
			SupplierThreadLocal<?> threadLocal = context.addSupplierThreadLocal(qualifier, objectType);

			// Register the Spring dependency
			springDependenciesByName.put(dependencyName, new SpringDependency(qualifier, objectType));

			// Provide the proxy to supplier thread local
			return Proxy.newProxyInstance(context.getClassLoader(), new Class[] { objectType },
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
		});

		// Load the Spring dependencies
		SpringDependency[] springDependencies = springDependenciesByName.values().stream()
				.toArray(SpringDependency[]::new);

		// Load listing of all the beans (mapped by their type)
		Map<Class<?>, List<String>> beanNamesByType = new HashMap<>();
		ConfigurableListableBeanFactory beanFactory = springContext.getBeanFactory();
		NEXT_BEAN: for (String name : springContext.getBeanDefinitionNames()) {
			BeanDefinition definition = beanFactory.getBeanDefinition(name);
			String beanClassName = definition.getBeanClassName();

			// Determine if factory method
			if (beanClassName == null) {
				if (definition instanceof AnnotatedBeanDefinition) {
					AnnotatedBeanDefinition annotatedDefinition = (AnnotatedBeanDefinition) definition;
					if (annotatedDefinition.getFactoryMethodMetadata() != null) {
						beanClassName = annotatedDefinition.getFactoryMethodMetadata().getReturnTypeName();
					}
				}
			}

			// Ensure have bean type
			if (beanClassName == null) {
				continue NEXT_BEAN;
			}

			// Load the bean type
			Class<?> beanType = context.loadClass(beanClassName);

			// Filter out Spring beans being loaded from OfficeFloor
			for (SpringDependency dependency : springDependencies) {
				if (beanType.isAssignableFrom(dependency.getObjectType())) {
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
			switch (beanNames.size()) {
			case 1:
				// Only the one type (so no qualifier)
				String singleBeanName = beanNames.get(0);
				context.addManagedObjectSource(null, beanType,
						new SpringBeanManagedObjectSource(singleBeanName, beanType, springContext, springDependencies));
				break;

			default:
				// Multiple, so provide qualifier
				for (String beanName : beanNames) {
					context.addManagedObjectSource(beanName, beanType,
							new SpringBeanManagedObjectSource(beanName, beanType, springContext, springDependencies));
				}
				break;
			}
		}
	}

}