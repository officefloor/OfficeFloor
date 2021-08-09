/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.source;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.logging.Logger;

import net.officefloor.frame.api.clock.Clock;

/**
 * Generic context for a source.
 * 
 * @author Daniel Sagenschneider
 */
public interface SourceContext extends SourceProperties {

	/**
	 * Obtains the name of the item being sourced.
	 * 
	 * @return Name of item being sourced.
	 */
	String getName();

	/**
	 * <p>
	 * Indicates if just loading as a type.
	 * <p>
	 * When loading as a type the configuration provided is disregarded. This allows
	 * sources to know when to load singleton configuration that will take effect.
	 * <p>
	 * Whether this is <code>true</code> or <code>false</code> the resulting type
	 * should be the same.
	 * 
	 * @return <code>true</code> if loading as a type.
	 */
	boolean isLoadingType();

	/**
	 * Obtains the listing of active profiles.
	 * 
	 * @return Active profiles.
	 */
	List<String> getProfiles();

	/**
	 * Attempts to load the specified {@link Class}.
	 * 
	 * @param name Name of the {@link Class}.
	 * @return {@link Class} or <code>null</code> if the {@link Class} can not be
	 *         found.
	 */
	Class<?> loadOptionalClass(String name);

	/**
	 * Loads the {@link Class}.
	 * 
	 * @param name Name of the {@link Class}.
	 * @return {@link Class}.
	 * @throws UnknownClassError If {@link Class} is not available. Let this
	 *                           propagate as OfficeFloor will handle it.
	 */
	Class<?> loadClass(String name) throws UnknownClassError;

	/**
	 * Attempts to obtain the resource at the specified location.
	 * 
	 * @param location Location of the resource.
	 * @return {@link InputStream} to the contents of the resource or
	 *         <code>null</code> if the resource can not be found.
	 */
	InputStream getOptionalResource(String location);

	/**
	 * Obtains the resource.
	 * 
	 * @param location Location of the resource.
	 * @return {@link InputStream} to the contents of the resource.
	 * @throws UnknownResourceError If resource is not found. Let this propagate as
	 *                              OfficeFloor will handle it.
	 */
	InputStream getResource(String location) throws UnknownResourceError;

	/**
	 * Loads the specific service.
	 * 
	 * @param <S>            Service type
	 * @param <F>            {@link ServiceFactory} type to create service.
	 * @param serviceFactory {@link ServiceFactory}.
	 * @return Service.
	 * @throws LoadServiceError If fails to load the service.
	 */
	<S, F extends ServiceFactory<S>> S loadService(F serviceFactory) throws LoadServiceError;

	/**
	 * Loads a single service.
	 * 
	 * @param <S>                   Service type
	 * @param <F>                   {@link ServiceFactory} type to create service.
	 * @param <D>                   Default {@link ServiceFactory} type.
	 * @param serviceFactoryType    Type of {@link ServiceFactory}.
	 * @param defaultServiceFactory Default {@link ServiceFactory} implementation.
	 *                              May be <code>null</code> to indicate no default
	 *                              service (one must be configured).
	 * @return Service.
	 * @throws UnknownServiceError If service is not configured and no default
	 *                             provided. Will also be thrown if more than one
	 *                             service is configured.
	 * @throws LoadServiceError    If fails to load the service.
	 */
	<S, F extends ServiceFactory<S>, D extends F> S loadService(Class<F> serviceFactoryType, D defaultServiceFactory)
			throws UnknownServiceError, LoadServiceError;

	/**
	 * Optionally loads a single service.
	 *
	 * @param <S>                Service type
	 * @param <F>                {@link ServiceFactory} type to create service.
	 * @param serviceFactoryType Type of {@link ServiceFactory}.
	 * @return Service or <code>null</code> if no service configured.
	 * @throws LoadServiceError If fails to load the service or
	 *                          {@link ServiceLoader} finds more than one service
	 *                          configured.
	 */
	<S, F extends ServiceFactory<S>> S loadOptionalService(Class<F> serviceFactoryType) throws LoadServiceError;

	/**
	 * Loads multiple services.
	 * 
	 * @param <S>                   Service type
	 * @param <F>                   {@link ServiceFactory} type to create service.
	 * @param <D>                   Default {@link ServiceFactory} type.
	 * @param serviceFactoryType    Type of {@link ServiceFactory}.
	 * @param defaultServiceFactory Default {@link ServiceFactory} implementation.
	 *                              May be <code>null</code> to indicate no default
	 *                              service.
	 * @return {@link Iterable} over the services. The {@link Iterable} may also
	 *         throw {@link LoadServiceError} if fails to create next service.
	 * @throws UnknownServiceError If no services are configured and no default
	 *                             provided.
	 * @throws LoadServiceError    If fails to load a service.
	 */
	<S, F extends ServiceFactory<S>, D extends F> Iterable<S> loadServices(Class<F> serviceFactoryType,
			D defaultServiceFactory) throws UnknownServiceError, LoadServiceError;

	/**
	 * Optionally loads multiple services.
	 * 
	 * @param <S>                Service type
	 * @param <F>                {@link ServiceFactory} type to create service.
	 * @param serviceFactoryType Type of {@link ServiceFactory}.
	 * @return {@link Iterable} over the services. May be no entries available. The
	 *         {@link Iterable} may also throw {@link LoadServiceError} if fails to
	 *         create next service.
	 * @throws LoadServiceError If fails to load a service.
	 */
	<S, F extends ServiceFactory<S>> Iterable<S> loadOptionalServices(Class<F> serviceFactoryType)
			throws LoadServiceError;

	/**
	 * <p>
	 * Obtains the {@link ClassLoader}.
	 * <p>
	 * This is only provided in specific cases where a {@link ClassLoader} is
	 * required (such as creating a {@link Proxy}). The other methods of this
	 * interface should be used in preference to the returned {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	ClassLoader getClassLoader();

	/**
	 * <p>
	 * Obtains the {@link Logger}.
	 * <p>
	 * The {@link Logger} is named specific to the source. This is especially the
	 * case for re-use of {@link Class} within sources, as would not be able to
	 * differentiate.
	 * 
	 * @return {@link Logger}.
	 */
	Logger getLogger();

	/**
	 * Obtains the {@link Clock}.
	 * 
	 * @param translator Translate the seconds since Epoch to "time" returned from
	 *                   the {@link Clock}.
	 * @return {@link Clock}.
	 */
	<T> Clock<T> getClock(Function<Long, T> translator);

}
