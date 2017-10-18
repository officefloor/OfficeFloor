/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.web.parameter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.tokenise.HttpRequestTokenHandler;
import net.officefloor.web.tokenise.HttpRequestTokeniseException;
import net.officefloor.web.tokenise.HttpRequestTokeniser;
import net.officefloor.web.value.load.ObjectInstantiator;
import net.officefloor.web.value.load.ValueLoader;
import net.officefloor.web.value.load.ValueLoaderFactory;
import net.officefloor.web.value.load.ValueLoaderSource;

/**
 * {@link HttpParametersLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoader<T> {

	/**
	 * <p>
	 * Mapping of specific {@link Class} to the {@link ValueLoaderFactory}
	 * instance to load values onto the object.
	 * <p>
	 * This is necessary as the concrete object may vary but still be of the
	 * type and as such will have varying {@link Method} instances to load the
	 * values.
	 * <p>
	 * Size is 2 as typically will only require type and concrete object.
	 */
	private final Map<Class<?>, ValueLoaderFactory<?>> typeToFactory = new ConcurrentHashMap<>(2);

	/**
	 * {@link ValueLoaderSource}.
	 */
	private ValueLoaderSource source;

	/**
	 * Obtains the {@link ValueLoaderFactory} instances for the class.
	 * 
	 * @param clazz
	 *            Type to extract the {@link ValueLoaderFactory}.
	 * @return {@link ValueLoaderFactory} for the class.
	 * @throws HttpParametersException
	 *             If fails to obtain the {@link ValueLoaderFactory}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <C> ValueLoaderFactory<C> getValueLoaderFactory(Class<C> clazz) throws HttpParametersException {

		// Lazy load the factory
		ValueLoaderFactory factory = this.typeToFactory.get(clazz);
		if (factory == null) {
			try {
				factory = this.source.sourceValueLoaderFactory(clazz);
				this.typeToFactory.put(clazz, factory);
			} catch (Exception ex) {
				throw new HttpParametersException(ex);
			}
		}

		// Return the factory
		return factory;
	}

	/**
	 * Initialises this {@link HttpParametersLoader}.
	 * 
	 * @param type
	 *            Type of object to be loaded (and may be an interface). The
	 *            type is interrogated for
	 *            <code>public void setXxx(String value)</code> methods for
	 *            loading corresponding parameters. The property name of each
	 *            method is the method name stripped of the leading
	 *            <code>set</code>.
	 * @param aliasMappings
	 *            Alias mappings so {@link HttpRequest} parameter names need not
	 *            match exactly the Object method property names.
	 * @param isCaseSensitive
	 *            Flag indicating if matching on parameter names is to be case
	 *            sensitive. Specifying <code>false</code> results in matching
	 *            names ignoring case - which makes for more tolerable loading.
	 * @param objectInstantiator
	 *            {@link ObjectInstantiator}.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public HttpParametersLoader(Class<T> type, Map<String, String> aliasMappings, boolean isCaseInsensitive,
			ObjectInstantiator objectInstantiator) throws Exception {

		// Provide empty alias mappings if null
		if (aliasMappings == null) {
			aliasMappings = Collections.emptyMap();
		}

		// Provide default object instantiator if null
		if (objectInstantiator == null) {
			objectInstantiator = new ObjectInstantiator() {
				@Override
				public <I> I instantiate(Class<I> clazz) throws Exception {
					return clazz.newInstance();
				}
			};
		}

		// Load the source
		this.source = new ValueLoaderSource(type, isCaseInsensitive, aliasMappings, objectInstantiator);
	}

	/**
	 * Loads the parameters of the {@link HttpRequest} to the Object.
	 * 
	 * @param <O>
	 *            Object type.
	 * @param httpRequest
	 *            {@link HttpRequest} to extract the parameters.
	 * @param object
	 *            Object to be loaded with the parameters.
	 * @throws IOException
	 *             If fails to read data from the {@link HttpRequest}.
	 * @throws HttpParametersException
	 *             If fails to load the {@link HttpRequest} parameters to the
	 *             Object.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <O extends T> void loadParameters(HttpRequest httpRequest, final O object)
			throws IOException, HttpParametersException {

		// Obtain the value loader
		ValueLoaderFactory factory = this.getValueLoaderFactory(object.getClass());
		ValueLoader loader;
		try {
			loader = factory.createValueLoader(object);
		} catch (Exception ex) {
			throw new HttpParametersException(ex);
		}
		final ValueLoader valueLoader = loader;

		// Parse the parameters and load onto the object
		try {
			HttpRequestTokeniser.tokeniseHttpRequest(httpRequest, new HttpRequestTokenHandler() {
				@Override
				public void handlePath(String path) throws HttpRequestTokeniseException {
					// Ignore path as only interested in parameters
				}

				@Override
				public void handleHttpParameter(String name, String value) throws HttpRequestTokeniseException {
					// Load the value
					try {
						valueLoader.loadValue(name, value);
					} catch (Exception ex) {
						throw new HttpRequestTokeniseException(ex);
					}
				}

				@Override
				public void handleQueryString(String queryString) throws HttpRequestTokeniseException {
					// Ignore query string as only interested in parameters
				}

				@Override
				public void handleFragment(String fragment) throws HttpRequestTokeniseException {
					// Ignore fragment as only interested in parameters
				}
			});
		} catch (HttpRequestTokeniseException ex) {
			throw new HttpParametersException(ex);
		}
	}

}