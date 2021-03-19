/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.source;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.source.LoadServiceError;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.api.source.UnknownServiceError;

/**
 * {@link SourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceContextImpl extends SourcePropertiesImpl implements SourceContext, ServiceContext {

	/**
	 * Delegate {@link SourceContext}.
	 */
	private final SourceContext delegate;

	/**
	 * Initiate the raw {@link SourceContext} to seed other {@link SourceContext}
	 * instances.
	 * 
	 * @param sourceName      Name of source.
	 * @param isLoadingType   Indicates if loading type.
	 * @param profiles        Active profiles.
	 * @param classLoader     {@link ClassLoader}.
	 * @param clockFactory    {@link ClockFactory}.
	 * @param resourceSources {@link ResourceSource} instances.
	 */
	public SourceContextImpl(String sourceName, boolean isLoadingType, String[] profiles, ClassLoader classLoader,
			ClockFactory clockFactory, ResourceSource... resourceSources) {
		this.delegate = new DelegateSourceContext(sourceName, isLoadingType, profiles, classLoader, clockFactory,
				resourceSources);
	}

	/**
	 * Initiate specific {@link SourceContext} with necessary
	 * {@link SourceProperties}.
	 * 
	 * @param sourceName         Name of source.
	 * @param isLoadingType      Indicates if loading type.
	 * @param additionalProfiles Additional profiles.
	 * @param delegate           Delegate {@link SourceContext}.
	 * @param sourceProperties   {@link SourceProperties}.
	 */
	public SourceContextImpl(String sourceName, boolean isLoadingType, String[] additionalProfiles,
			SourceContext delegate, SourceProperties sourceProperties) {
		super(sourceProperties);
		this.delegate = new DelegateWrapSourceContext(sourceName, isLoadingType, additionalProfiles, delegate);
	}

	/*
	 * ================== SourceContext =======================
	 */

	@Override
	public String getName() {
		return this.delegate.getName();
	}

	@Override
	public boolean isLoadingType() {
		return this.delegate.isLoadingType();
	}

	@Override
	public List<String> getProfiles() {
		return this.delegate.getProfiles();
	}

	@Override
	public Class<?> loadOptionalClass(String name) {
		return this.delegate.loadOptionalClass(name);
	}

	@Override
	public Class<?> loadClass(String name) throws UnknownClassError {
		return this.delegate.loadClass(name);
	}

	@Override
	public InputStream getOptionalResource(String location) {
		return this.delegate.getOptionalResource(location);
	}

	@Override
	public InputStream getResource(String location) throws UnknownResourceError {
		return this.delegate.getResource(location);
	}

	@Override
	public <S, F extends ServiceFactory<S>> S loadService(F serviceFactory) throws LoadServiceError {
		return createService(serviceFactory, this);
	}

	@Override
	public <S, F extends ServiceFactory<S>, D extends F> S loadService(Class<F> serviceFactoryType,
			D defaultServiceFactory) throws UnknownServiceError, LoadServiceError {
		return loadService(serviceFactoryType, defaultServiceFactory, this);
	}

	@Override
	public <S, F extends ServiceFactory<S>> S loadOptionalService(Class<F> serviceFactoryType) throws LoadServiceError {
		return loadOptionalService(serviceFactoryType, this);
	}

	@Override
	public <S, F extends ServiceFactory<S>, D extends F> Iterable<S> loadServices(Class<F> serviceFactoryType,
			D defaultServiceFactory) throws UnknownServiceError, LoadServiceError {
		return loadServices(serviceFactoryType, defaultServiceFactory, this);
	}

	@Override
	public <S, F extends ServiceFactory<S>> Iterable<S> loadOptionalServices(Class<F> serviceFactoryType)
			throws LoadServiceError {
		return loadOptionalServices(serviceFactoryType, this);
	}

	@Override
	public <T> Clock<T> getClock(Function<Long, T> translator) {
		return this.delegate.getClock(translator);
	}

	@Override
	public Logger getLogger() {
		return this.delegate.getLogger();
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.delegate.getClassLoader();
	}

	/**
	 * Wraps a delegate {@link SourceContext}.
	 */
	private static class DelegateWrapSourceContext implements SourceContext {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Indicates if loading type.
		 */
		private final boolean isLoadingType;

		/**
		 * Active profiles.
		 */
		private final List<String> activeProfiles;

		/**
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * {@link SourceContext}.
		 */
		private final SourceContext delegate;

		/**
		 * Initiate.
		 * 
		 * @param sourceName         Name of source.
		 * @param isLoadingType      Indicates if loading type.
		 * @param additionalProfiles Additional profiles.
		 * @param delegate           Delegate {@link SourceContext}.
		 */
		private DelegateWrapSourceContext(String sourceName, boolean isLoadingType, String[] additionalProfiles,
				SourceContext delegate) {
			this.name = sourceName;
			this.isLoadingType = isLoadingType;
			this.delegate = delegate;
			this.logger = OfficeFrame.getLogger(sourceName);

			// Create listing of profiles
			List<String> profiles = new ArrayList<>();
			Consumer<String> profileLoader = (profile) -> {
				if (!profiles.contains(profile)) {
					profiles.add(profile);
				}
			};
			if (additionalProfiles != null) {
				Arrays.asList(additionalProfiles).forEach(profileLoader);
			}
			this.delegate.getProfiles().forEach(profileLoader);
			this.activeProfiles = Collections.unmodifiableList(profiles);
		}

		/*
		 * =================== SourceContext ====================
		 */

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean isLoadingType() {
			return this.isLoadingType;
		}

		@Override
		public List<String> getProfiles() {
			return this.activeProfiles;
		}

		@Override
		public String[] getPropertyNames() {
			return this.delegate.getPropertyNames();
		}

		@Override
		public String getProperty(String name) throws UnknownPropertyError {
			return this.delegate.getProperty(name);
		}

		@Override
		public String getProperty(String name, String defaultValue) {
			return this.delegate.getProperty(name, defaultValue);
		}

		@Override
		public Properties getProperties() {
			return this.delegate.getProperties();
		}

		@Override
		public Class<?> loadOptionalClass(String name) {
			return this.delegate.loadOptionalClass(name);
		}

		@Override
		public Class<?> loadClass(String name) throws UnknownClassError {
			return this.delegate.loadClass(name);
		}

		@Override
		public InputStream getOptionalResource(String location) {
			return this.delegate.getOptionalResource(location);
		}

		@Override
		public InputStream getResource(String location) throws UnknownResourceError {
			return this.delegate.getResource(location);
		}

		@Override
		public <T> Clock<T> getClock(Function<Long, T> translator) {
			return this.delegate.getClock(translator);
		}

		@Override
		public Logger getLogger() {
			return this.logger;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.delegate.getClassLoader();
		}

		@Override
		public <S, F extends ServiceFactory<S>> S loadService(F serviceFactory) throws LoadServiceError {
			return this.delegate.loadService(serviceFactory);
		}

		@Override
		public <S, F extends ServiceFactory<S>, D extends F> S loadService(Class<F> serviceFactoryType,
				D defaultServiceFactory) throws UnknownServiceError, LoadServiceError {
			return this.delegate.loadService(serviceFactoryType, defaultServiceFactory);
		}

		@Override
		public <S, F extends ServiceFactory<S>> S loadOptionalService(Class<F> serviceFactoryType)
				throws LoadServiceError {
			return this.delegate.loadOptionalService(serviceFactoryType);
		}

		@Override
		public <S, F extends ServiceFactory<S>, D extends F> Iterable<S> loadServices(Class<F> serviceFactoryType,
				D defaultServiceFactory) throws UnknownServiceError, LoadServiceError {
			return this.delegate.loadServices(serviceFactoryType, defaultServiceFactory);
		}

		@Override
		public <S, F extends ServiceFactory<S>> Iterable<S> loadOptionalServices(Class<F> serviceFactoryType)
				throws LoadServiceError {
			return this.delegate.loadOptionalServices(serviceFactoryType);
		}
	}

	/**
	 * Delegate {@link SourceContext}.
	 */
	private static class DelegateSourceContext extends SourcePropertiesImpl implements SourceContext, ServiceContext {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Indicates if loading type.
		 */
		private final boolean isLoadingType;

		/**
		 * Active profiles.
		 */
		private final List<String> activeProfiles;

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * {@link ClockFactory}.
		 */
		private final ClockFactory clockFactory;

		/**
		 * {@link ResourceSource} instances.
		 */
		private final ResourceSource[] resourceSources;

		/**
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * Initiate.
		 * 
		 * @param sourceName      Source name.
		 * @param isLoadingType   Indicates if loading type.
		 * @param activeProfiles  Active profiles.
		 * @param classLoader     {@link ClassLoader}.
		 * @param clockFactory    {@link ClockFactory}.
		 * @param resourceSources {@link ResourceSource} instances.
		 */
		private DelegateSourceContext(String sourceName, boolean isLoadingType, String[] activeProfiles,
				ClassLoader classLoader, ClockFactory clockFactory, ResourceSource[] resourceSources) {
			this.name = sourceName;
			this.isLoadingType = isLoadingType;
			this.activeProfiles = ((activeProfiles == null) || (activeProfiles.length == 0)) ? Collections.emptyList()
					: Collections.unmodifiableList(Arrays.asList(activeProfiles));
			this.classLoader = classLoader;
			this.clockFactory = clockFactory;
			this.resourceSources = resourceSources;
			this.logger = OfficeFrame.getLogger(sourceName);
		}

		/*
		 * =================== SourceContext ====================
		 */

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean isLoadingType() {
			return this.isLoadingType;
		}

		@Override
		public List<String> getProfiles() {
			return this.activeProfiles;
		}

		@Override
		public Class<?> loadOptionalClass(String name) {
			try {

				// Determine if primitive
				switch (name) {
				case "boolean":
					return boolean.class;
				case "byte":
					return byte.class;
				case "short":
					return short.class;
				case "char":
					return char.class;
				case "int":
					return int.class;
				case "long":
					return long.class;
				case "float":
					return float.class;
				case "double":
					return double.class;
				case "[Z":
					return boolean[].class;
				case "[B":
					return byte[].class;
				case "[S":
					return short[].class;
				case "[C":
					return char[].class;
				case "[I":
					return int[].class;
				case "[J":
					return long[].class;
				case "[F":
					return float[].class;
				case "[D":
					return double[].class;
				default:
					// Carry on to load class
					break;
				}

				// Determine if array
				final String START = "[L";
				final String END = ";";
				if (name.startsWith(START) && name.endsWith(END)) {

					// Array, so obtain component name
					String componentName = name.substring(START.length(), name.length() - END.length());
					Class<?> componentType = this.classLoader.loadClass(componentName);

					// Return the array class
					return Array.newInstance(componentType, 0).getClass();
				}

				// Load the non-array class
				return this.classLoader.loadClass(name);

			} catch (ClassNotFoundException ex) {
				return null;
			}
		}

		@Override
		public Class<?> loadClass(String name) throws UnknownClassError {

			// Load the class
			Class<?> clazz = this.loadOptionalClass(name);

			// Ensure have class
			if (clazz == null) {
				throw new UnknownClassError(name);
			}

			// Return the class
			return clazz;
		}

		@Override
		public InputStream getOptionalResource(String location) {

			// Attempt to obtain from resource sources first
			for (ResourceSource resourceSource : this.resourceSources) {
				InputStream resource = resourceSource.sourceResource(location);
				if (resource != null) {
					return resource; // located resource
				}
			}

			// Attempt to load from class loader
			InputStream resource = this.classLoader.getResourceAsStream(location);

			// Return the resource
			return resource;
		}

		@Override
		public InputStream getResource(String location) throws UnknownResourceError {

			// Attempt to obtain the resource
			InputStream resource = this.getOptionalResource(location);

			// Ensure have resource
			if (resource == null) {
				throw new UnknownResourceError(location);
			}

			// Return the resource
			return resource;
		}

		@Override
		public <S, F extends ServiceFactory<S>> S loadService(F serviceFactory) throws LoadServiceError {
			return createService(serviceFactory, this);
		}

		@Override
		public <S, F extends ServiceFactory<S>, D extends F> S loadService(Class<F> serviceFactoryType,
				D defaultServiceFactory) throws UnknownServiceError, LoadServiceError {
			return SourceContextImpl.loadService(serviceFactoryType, defaultServiceFactory, this);
		}

		@Override
		public <S, F extends ServiceFactory<S>> S loadOptionalService(Class<F> serviceFactoryType)
				throws LoadServiceError {
			return SourceContextImpl.loadOptionalService(serviceFactoryType, this);
		}

		@Override
		public <S, F extends ServiceFactory<S>, D extends F> Iterable<S> loadServices(Class<F> serviceFactoryType,
				D defaultServiceFactory) throws UnknownServiceError, LoadServiceError {
			return SourceContextImpl.loadServices(serviceFactoryType, defaultServiceFactory, this);
		}

		@Override
		public <S, F extends ServiceFactory<S>> Iterable<S> loadOptionalServices(Class<F> serviceFactoryType)
				throws LoadServiceError {
			return SourceContextImpl.loadOptionalServices(serviceFactoryType, this);
		}

		@Override
		public <T> Clock<T> getClock(Function<Long, T> translator) {
			return this.clockFactory.createClock(translator);
		}

		@Override
		public Logger getLogger() {
			return this.logger;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

	/**
	 * Loads the service.
	 * 
	 * @param <S>                   Service type.
	 * @param <F>                   {@link ServiceFactory} type.
	 * @param <D>                   Default {@link ServiceFactory} type.
	 * @param serviceFactoryType    {@link ServiceFactory} type.
	 * @param defaultServiceFactory Default {@link ServiceFactory} type.
	 * @param context               {@link ServiceContext}.
	 * @return Service.
	 * @throws UnknownServiceError If unknown service.
	 * @throws LoadServiceError    If fails to load service.
	 */
	private static <S, F extends ServiceFactory<S>, D extends F> S loadService(Class<F> serviceFactoryType,
			D defaultServiceFactory, ServiceContext context) throws UnknownServiceError, LoadServiceError {

		// Attempt to load the service
		S service = loadOptionalService(serviceFactoryType, context);
		if (service != null) {
			return service;
		}

		// Provide default service (if available)
		if (defaultServiceFactory != null) {
			return createService(defaultServiceFactory, context);
		}

		// No configured service
		throw new UnknownServiceError(serviceFactoryType);
	}

	/**
	 * Loads the optional service.
	 * 
	 * @param <S>                Service type.
	 * @param <F>                {@link ServiceFactory} type.
	 * @param serviceFactoryType {@link ServiceFactory} type.
	 * @param context            {@link ServiceContext}.
	 * @return Optional service. May be <code>null</code>.
	 * @throws LoadServiceError If fails to load service.
	 */
	private static <S, F extends ServiceFactory<S>> S loadOptionalService(Class<F> serviceFactoryType,
			ServiceContext context) throws LoadServiceError {

		// Obtain the service factories
		Iterator<F> serviceFactories = loadServiceFactories(serviceFactoryType, context.getClassLoader());
		if (!serviceFactories.hasNext()) {
			return null; // no service configured
		}

		// Obtain the service
		S service = nextService(serviceFactories, serviceFactoryType, context);

		// Ensure only the one service factory configured
		if (serviceFactories.hasNext()) {
			throw new LoadServiceError(serviceFactoryType.getName(), new Exception(
					"Multiple services configured for single required service " + serviceFactoryType.getName()));
		}

		// Return the service
		return service;
	}

	/**
	 * Loads the services.
	 * 
	 * @param <S>                   Service type.
	 * @param <F>                   {@link ServiceFactory} type.
	 * @param <D>                   Default {@link ServiceFactory} type.
	 * @param serviceFactoryType    {@link ServiceFactory} type.
	 * @param defaultServiceFactory Default {@link ServiceFactory} type.
	 * @param context               {@link ServiceContext}.
	 * @return {@link Iterable} over the services.
	 * @throws UnknownServiceError If no service.
	 * @throws LoadServiceError    If failure in loading service.
	 */
	private static <S, F extends ServiceFactory<S>, D extends F> Iterable<S> loadServices(Class<F> serviceFactoryType,
			D defaultServiceFactory, ServiceContext context) throws UnknownServiceError, LoadServiceError {

		// Obtain the service factories
		Iterator<F> serviceFactories = loadServiceFactories(serviceFactoryType, context.getClassLoader());
		if (serviceFactories.hasNext()) {
			// Services configured
			return () -> new ServiceIterator<>(serviceFactories, serviceFactoryType, context);
		}

		// No services configured, so attempt default service
		if (defaultServiceFactory != null) {
			return () -> new DefaultServiceIterator<>(defaultServiceFactory, context);
		}

		// No configured services
		throw new UnknownServiceError(serviceFactoryType);
	}

	/**
	 * Loads the optional services.
	 * 
	 * @param <S>                Service type.
	 * @param <F>                {@link ServiceFactory} type.
	 * @param serviceFactoryType {@link ServiceFactory} type.
	 * @param context            {@link ServiceContext}.
	 * @return {@link Iterable} over the optional services.
	 * @throws LoadServiceError If fails to load service.
	 */
	private static <S, F extends ServiceFactory<S>> Iterable<S> loadOptionalServices(Class<F> serviceFactoryType,
			ServiceContext context) throws LoadServiceError {

		// Obtain the service factories
		Iterator<F> serviceFactories = loadServiceFactories(serviceFactoryType, context.getClassLoader());

		// Return iterator over the services
		return () -> new ServiceIterator<>(serviceFactories, serviceFactoryType, context);
	}

	/**
	 * Loads the {@link Iterator} over the {@link ServiceFactory} instances.
	 * 
	 * @param serviceFactoryType Type of {@link ServiceFactory}.
	 * @param classLoader        {@link ClassLoader}.
	 * @return {@link Iterator} over the {@link ServiceFactory} instances.
	 */
	private static <S, F extends ServiceFactory<S>> Iterator<F> loadServiceFactories(Class<F> serviceFactoryType,
			ClassLoader classLoader) {
		return ServiceLoader.load(serviceFactoryType, classLoader).iterator();
	}

	/**
	 * Loads the next service from the {@link Iterator}.
	 * 
	 * @param serviceFactories {@link Iterator} over the {@link ServiceFactory}
	 *                         instances.
	 * @param serviceContext   {@link ServiceContext}.
	 * @return Next service.
	 */
	private static <S, F extends ServiceFactory<S>> S nextService(Iterator<F> serviceFactories,
			Class<F> serviceFactoryType, ServiceContext serviceContext) {

		// Obtain the next service factory
		F serviceFactory;
		try {
			serviceFactory = serviceFactories.next();
		} catch (ServiceConfigurationError error) {
			Throwable cause = error.getCause();
			throw new LoadServiceError(serviceFactoryType.getName(), cause != null ? cause : error);
		}

		// Create the service
		return createService(serviceFactory, serviceContext);
	}

	/**
	 * Creates the service from the {@link ServiceFactory}.
	 * 
	 * @param serviceFactory {@link ServiceFactory}.
	 * @param serviceContext {@link ServiceContext}.
	 * @return Service.
	 */
	private static <S> S createService(ServiceFactory<S> serviceFactory, ServiceContext serviceContext) {

		// Create the service
		S service;
		try {
			service = serviceFactory.createService(serviceContext);

		} catch (UnknownPropertyError ex) {
			// Propagate as unknown property for service
			throw new UnknownPropertyError(ex, serviceFactory);

		} catch (Throwable ex) {
			// Propagate as load error
			throw new LoadServiceError(serviceFactory.getClass().getName(), ex);
		}

		// Ensure have service
		if (service == null) {
			throw new LoadServiceError(serviceFactory.getClass().getName(),
					new Exception("No service created from " + serviceFactory.getClass().getName()));
		}

		// Return the service
		return service;
	}

	/**
	 * Service {@link Iterator}.
	 */
	private static class ServiceIterator<S, F extends ServiceFactory<S>> implements Iterator<S> {

		/**
		 * {@link Iterator} over the {@link ServiceFactory} instances.
		 */
		private final Iterator<F> serviceFactories;

		/**
		 * Type of {@link ServiceFactory}.
		 */
		private final Class<F> serviceFactoryType;

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext serviceContext;

		/**
		 * Instantiate.
		 * 
		 * @param serviceFactories   {@link Iterator} over the {@link ServiceFactory}
		 *                           instances.
		 * @param serviceFactoryType Type of {@link ServiceFactory}.
		 * @param serviceContext     {@link ServiceContext}.
		 */
		private ServiceIterator(Iterator<F> serviceFactories, Class<F> serviceFactoryType,
				ServiceContext serviceContext) {
			this.serviceFactories = serviceFactories;
			this.serviceFactoryType = serviceFactoryType;
			this.serviceContext = serviceContext;
		}

		/*
		 * ================= Iterator ===================
		 */

		@Override
		public boolean hasNext() {
			return this.serviceFactories.hasNext();
		}

		@Override
		public S next() {
			return nextService(this.serviceFactories, serviceFactoryType, this.serviceContext);
		}
	}

	/**
	 * Default service {@link Iterator}.
	 */
	private static class DefaultServiceIterator<S, F extends ServiceFactory<S>> implements Iterator<S> {

		/**
		 * {@link ServiceFactory}.
		 */
		private F defaultServiceFactory;

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext serviceContext;

		/**
		 * Instantiate.
		 * 
		 * @param defaultServiceFactory Default {@link ServiceFactory}.
		 * @param serviceContext        {@link ServiceContext}.
		 */
		private DefaultServiceIterator(F defaultServiceFactory, ServiceContext serviceContext) {
			this.defaultServiceFactory = defaultServiceFactory;
			this.serviceContext = serviceContext;
		}

		/*
		 * ================= Iterator ===================
		 */

		@Override
		public boolean hasNext() {
			return this.defaultServiceFactory != null;
		}

		@Override
		public S next() {

			// Ensure have next
			if (this.defaultServiceFactory == null) {
				throw new NoSuchElementException();
			}

			// Create the service
			S service = createService(this.defaultServiceFactory, serviceContext);

			// Clear default, as is next
			this.defaultServiceFactory = null;

			// Return the service
			return service;
		}
	}

}
