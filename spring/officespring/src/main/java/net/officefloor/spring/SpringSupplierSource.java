/*-
 * #%L
 * Spring Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.impl.structure.SupplierThreadLocalNodeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.dependency.OfficeFloorThreadLocalDependency;
import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.plugin.section.clazz.PropertyValue;
import net.officefloor.spring.extension.AfterSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.BeforeSpringLoadSupplierExtensionContext;
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
	 * {@link PropertyValue} to configure active Spring profiles.
	 */
	public static final String PROPERTY_ACTIVE_PROFILES = "profiles";

	/**
	 * {@link PropertyValue} to flag whether to unlink Spring profiles to
	 * {@link Office} profiles.
	 */
	public static final String PROPERTY_UNLINK_CONTEXT_PROFILES = "unlink.officefloor.profiles";

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
	 * @param <O>        Object type.
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
			throw new FatalBeanException(
					"Failed to obtain " + OfficeFloor.class.getSimpleName() + " dependency "
							+ SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, objectType.getName()),
					ex);
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
	 * @param <S>     Loaded context.
	 * @param <E>     Possible {@link Throwable} from loading.
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
	 * @param additionalPropertyNameValuePairs Additional {@link PropertyValue}
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
	 * Forces starting Spring.
	 * 
	 * @param availableTypes {@link AvailableType} instances.
	 * @return {@link ConfigurableApplicationContext}. <code>null</code> if already
	 *         started.
	 * @throws Exception If fails to start Spring.
	 */
	public static ConfigurableApplicationContext forceStartSpring(AvailableType[] availableTypes) throws Exception {

		// Attempt complete if not already completed
		SpringSupplierSource source = supplierSource.get();
		if (source != null) {

			// Complete to start Spring
			source.complete(availableTypes);

			// Return Spring
			return source.springContext;

		} else {
			// Already completed
			return null;
		}
	}

	/**
	 * Name of {@link PropertyValue} for the Spring Boot configuration
	 * {@link Class}.
	 */
	public static final String CONFIGURATION_CLASS_NAME = "configuration.class";

	/**
	 * {@link ThreadLocal} for the {@link SpringSupplierSource}.
	 */
	private static final ThreadLocal<SpringSupplierSource> supplierSource = new ThreadLocal<>();

	/**
	 * {@link SpringCompletion}.
	 */
	private SpringCompletion springCompletion;

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext springContext;

	/**
	 * Completes the loading of Spring.
	 * 
	 * @param availableTypes {@link AvailableType} instances.
	 * @throws Exception If fails to load Spring.
	 */
	private void complete(AvailableType[] availableTypes) throws Exception {

		// Determine if already completed
		if (this.springCompletion == null) {
			return; // already completed
		}

		// Avoid recursive completion
		SpringCompletion completion = this.springCompletion;
		this.springCompletion = null;

		// Complete
		completion.complete(availableTypes);
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

		// Register to allow forced completion
		supplierSource.set(this);

		// Load the extensions
		List<SpringSupplierExtension> extensions = new LinkedList<>();
		for (SpringSupplierExtension extension : context
				.loadOptionalServices(SpringSupplierExtensionServiceFactory.class)) {
			extensions.add(extension);
		}

		// Provide completion of Spring
		this.springCompletion = new SpringCompletion(extensions, context);
		context.addCompileCompletion((completion) -> {

			// Attempt to complete
			AvailableType[] availableTypes = completion.getAvailableTypes();
			this.complete(availableTypes);

			// Remove ability to force complete
			supplierSource.remove();
		});
	}

	@Override
	public void terminate() {

		// Close the spring context
		this.springContext.close();
	}

	/**
	 * Completion of Spring loading.
	 */
	private class SpringCompletion {

		/**
		 * {@link SpringSupplierExtension} instances.
		 */
		private final List<SpringSupplierExtension> extensions;

		/**
		 * {@link SupplierSourceContext}.
		 */
		private final SupplierSourceContext context;

		/**
		 * Instantiate.
		 * 
		 * @param extensions {@link SpringSupplierExtension} instances.
		 * @param context    {@link SupplierSourceContext}.
		 */
		private SpringCompletion(List<SpringSupplierExtension> extensions, SupplierSourceContext context) {
			this.extensions = extensions;
			this.context = context;
		}

		/**
		 * Completes the load.
		 * 
		 * @param availableTypes {@link AvailableType} instances.
		 */
		private void complete(AvailableType[] availableTypes) throws Exception {

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
				SupplierThreadLocal<?> threadLocal = this.context.addSupplierThreadLocal(qualifier, objectType);

				// Register the Spring dependency
				springDependencies.put(dependencyName, new SpringDependency(qualifier, objectType));

				// Create the dependency to supplier thread local
				dependency = OfficeFloorThreadLocalDependency.newStaticProxy(objectType, this.context.getClassLoader(),
						() -> {
							Object object = threadLocal.get();
							if (object == null) {
								throw new IllegalStateException(OfficeFloor.class.getSimpleName()
										+ " supplied bean for " + dependencyName + " is not available");
							}
							return object;
						});

				// Register the dependency and return it
				existingSpringDependencies.put(dependencyName, dependency);
				return dependency;
			};

			// Obtain the spring profiles
			List<String> profilesList = new ArrayList<>();
			String activeProfiles = this.context.getProperty(PROPERTY_ACTIVE_PROFILES, null);
			if (activeProfiles != null) {
				for (String profile : activeProfiles.split(",")) {
					if (!CompileUtil.isBlank(profile)) {
						profilesList.add(profile.trim());
					}
				}
			}
			boolean isUnlinkProfiles = Boolean
					.parseBoolean(this.context.getProperty(PROPERTY_UNLINK_CONTEXT_PROFILES, String.valueOf(false)));
			if (!isUnlinkProfiles) {
				for (String profile : this.context.getProfiles()) {
					profilesList.add(profile);
				}
			}
			String[] profiles = profilesList.toArray(new String[profilesList.size()]);

			// Load Spring with access to hook in OfficeFloor managed objects
			ConfigurableApplicationContext springContext = runInContext(() -> {

				// Run before spring load
				BeforeSpringLoadSupplierExtensionContext beforeContext = new BeforeSpringLoadSupplierExtensionContextImpl(
						dependencyFactory, this.context, availableTypes);
				for (SpringSupplierExtension extension : this.extensions) {
					extension.beforeSpringLoad(beforeContext);
				}

				// Load the configurable application context
				String configurationClassName = this.context.getProperty(CONFIGURATION_CLASS_NAME);
				Class<?> configurationClass = this.context.loadClass(configurationClassName);
				SpringApplicationBuilder springBuilder = new SpringApplicationBuilder(configurationClass);

				// Enable extension to building Spring
				for (SpringSupplierExtension extension : this.extensions) {
					extension.configureSpring(springBuilder);
				}

				// Build the application context
				ConfigurableApplicationContext applicationContext = springBuilder.profiles(profiles).run();

				// Run after spring load
				AfterSpringLoadSupplierExtensionContext afterContext = new AfterSpringLoadSupplierExtensionContextImpl(
						dependencyFactory, this.context, availableTypes, applicationContext);
				for (SpringSupplierExtension extension : this.extensions) {
					extension.afterSpringLoad(afterContext);
				}

				// Return the application context
				return applicationContext;

			}, dependencyFactory);

			// Make available to close Spring
			SpringSupplierSource.this.springContext = springContext;

			// Make available to inject
			context.addManagedObjectSource(null, springContext.getClass(),
					new ApplicationContextManagedObjectSource(springContext));

			// Provide all spring beans
			context.addInternalSupplier(new InternalSupplier() {

				@Override
				public boolean isObjectAvailable(String qualifier, Class<?> objectType) {
					if (qualifier != null) {
						return false; // bean's not qualified
					}
					try {
						return this.getBean(objectType) != null;
					} catch (Exception ex) {
						return false;
					}
				}

				@Override
				public <O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user)
						throws UnknownObjectException {
					if (qualifier != null) {
						throw new UnknownObjectException("qualified spring bean by qualifier " + qualifier);
					}
					try {
						O bean = this.getBean(objectType);
						user.use(bean, null);
					} catch (NoSuchBeanDefinitionException ex) {
						throw new UnknownObjectException(objectType.getName() + " spring bean: " + ex.getMessage());
					} catch (Exception ex) {
						user.use(null, ex);
					}
				}

				private <O> O getBean(Class<O> objectType) {
					return springContext.getBean(objectType);
				}
			});

			// Determine if capture the application context
			Consumer<ConfigurableApplicationContext> captureApplicationContext = applicationContextCapture.get();
			if (captureApplicationContext != null) {
				captureApplicationContext.accept(SpringSupplierSource.this.springContext);
			}

			// Load listing of all the beans (mapped by their type)
			Map<Class<?>, List<String>> beanNamesByType = new HashMap<>();
			NEXT_BEAN: for (String name : springContext.getBeanDefinitionNames()) {

				// Load the bean type
				Class<?> beanType = springContext.getBean(name).getClass();

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
					springDependenciesList = this.decorateBean(singleBeanName, beanType, this.extensions,
							springDependencies);

					// Load the single (unqualified) bean
					this.context.addManagedObjectSource(null, beanType, new SpringBeanManagedObjectSource(
							singleBeanName, beanType, springContext, springDependenciesList));
					break;

				default:
					// Multiple, so provide qualifier
					for (String beanName : beanNames) {

						// Decorate the bean
						springDependenciesList = this.decorateBean(beanName, beanType, this.extensions,
								springDependencies);

						// Load the qualified bean
						this.context.addManagedObjectSource(beanName, beanType, new SpringBeanManagedObjectSource(
								beanName, beanType, springContext, springDependenciesList));
					}
					break;
				}
			}
		}

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
	}

	/**
	 * Abstract {@link SpringSupplierExtensionContext}.
	 */
	private static class AbstractSpringSupplierExtensionContext implements SpringSupplierExtensionContext {

		/**
		 * {@link SpringDependencyFactory}.
		 */
		private final SpringDependencyFactory dependencyFactory;

		/**
		 * {@link SupplierSourceContext}.
		 */
		private final SupplierSourceContext context;

		/**
		 * {@link AvailableType} instances.
		 */
		private final AvailableType[] availableTypes;

		/**
		 * Instantiate.
		 * 
		 * @param dependencyFactory {@link SpringDependencyFactory}.
		 * @param context           {@link SupplierSourceContext}.
		 * @param availableTypes    {@link AvailableType} instances.
		 */
		protected AbstractSpringSupplierExtensionContext(SpringDependencyFactory dependencyFactory,
				SupplierSourceContext context, AvailableType[] availableTypes) {
			this.dependencyFactory = dependencyFactory;
			this.context = context;
			this.availableTypes = availableTypes;
		}

		/*
		 * ===================== SpringSupplierExtensionContext =====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public <O> O getManagedObject(String qualifier, Class<? extends O> objectType) throws Exception {
			return (O) dependencyFactory.createDependency(qualifier, objectType);
		}

		@Override
		public void addThreadSynchroniser(ThreadSynchroniserFactory threadSynchroniserFactory) {
			this.context.addThreadSynchroniser(threadSynchroniserFactory);
		}

		@Override
		public AvailableType[] getAvailableTypes() {
			return this.availableTypes;
		}
	}

	/**
	 * {@link BeforeSpringLoadSupplierExtensionContext} implementation.
	 */
	private static class BeforeSpringLoadSupplierExtensionContextImpl extends AbstractSpringSupplierExtensionContext
			implements BeforeSpringLoadSupplierExtensionContext {

		/**
		 * Instantiate.
		 * 
		 * @param dependencyFactory {@link SpringDependencyFactory}.
		 * @param context           {@link SupplierSourceContext}.
		 * @param availableTypes    {@link AvailableType} instances.
		 */
		private BeforeSpringLoadSupplierExtensionContextImpl(SpringDependencyFactory dependencyFactory,
				SupplierSourceContext context, AvailableType[] availableTypes) {
			super(dependencyFactory, context, availableTypes);
		}
	}

	/**
	 * {@link AfterSpringLoadSupplierExtensionContext} implementation.
	 */
	private static class AfterSpringLoadSupplierExtensionContextImpl extends AbstractSpringSupplierExtensionContext
			implements AfterSpringLoadSupplierExtensionContext {

		/**
		 * {@link ConfigurableApplicationContext}.
		 */
		private final ConfigurableApplicationContext springContext;

		/**
		 * Instantiate.
		 * 
		 * @param dependencyFactory {@link SpringDependencyFactory}.
		 * @param context           {@link SupplierSourceContext}.
		 * @param availableTypes    {@link AvailableType} instances.
		 * @param springContext     {@link ConfigurableApplicationContext}.
		 */
		private AfterSpringLoadSupplierExtensionContextImpl(SpringDependencyFactory dependencyFactory,
				SupplierSourceContext context, AvailableType[] availableTypes,
				ConfigurableApplicationContext springContext) {
			super(dependencyFactory, context, availableTypes);
			this.springContext = springContext;
		}

		/*
		 * =================== AfterSpringLoadSupplierExtensionContext =================
		 */

		@Override
		public ConfigurableApplicationContext getSpringContext() {
			return this.springContext;
		}
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
