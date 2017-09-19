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
package net.officefloor.plugin.web.http.parameters;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.value.loader.ObjectInstantiator;
import net.officefloor.plugin.value.loader.ValueLoader;
import net.officefloor.plugin.value.loader.ValueLoaderFactory;
import net.officefloor.plugin.value.loader.ValueLoaderSource;
import net.officefloor.plugin.value.loader.ValueLoaderSourceImpl;
import net.officefloor.plugin.web.http.parameters.HttpParametersException;
import net.officefloor.plugin.web.http.parameters.HttpParametersLoader;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokenHandler;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniser;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniserImpl;
import net.officefloor.server.http.HttpRequest;

/**
 * {@link HttpParametersLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderImpl<T> implements HttpParametersLoader<T> {

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
	private final Map<Class<?>, ValueLoaderFactory<?>> typeToFactory = new HashMap<Class<?>, ValueLoaderFactory<?>>(
			2);

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
	private <C> ValueLoaderFactory<C> getValueLoaderFactory(Class<C> clazz)
			throws HttpParametersException {

		// Ensure thread-safe in lazy loading
		synchronized (this.typeToFactory) {

			// Lazy load the factory
			ValueLoaderFactory factory = this.typeToFactory.get(clazz);
			if (factory == null) {
				try {
					factory = this.source.sourceValueLoaderFactory(clazz);
				} catch (Exception ex) {
					throw new HttpParametersException(ex);
				}
			}

			// Return the factory
			return factory;
		}
	}

	/*
	 * ==================== HttpParametersLoader =========================
	 */

	@Override
	public void init(Class<T> type, Map<String, String> aliasMappings,
			boolean isCaseInsensitive, ObjectInstantiator objectInstantiator)
			throws Exception {

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
		this.source = new ValueLoaderSourceImpl();
		this.source.init(type, isCaseInsensitive, aliasMappings,
				objectInstantiator);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <O extends T> void loadParameters(HttpRequest httpRequest,
			final O object) throws IOException, HttpParametersException {

		// Obtain the value loader
		ValueLoaderFactory factory = this.getValueLoaderFactory(object
				.getClass());
		ValueLoader loader;
		try {
			loader = factory.createValueLoader(object);
		} catch (Exception ex) {
			throw new HttpParametersException(ex);
		}
		final ValueLoader valueLoader = loader;

		// Create the tokeniser
		HttpRequestTokeniser tokeniser = new HttpRequestTokeniserImpl();

		// Parse the parameters and load onto the object
		tokeniser.tokeniseHttpRequest(httpRequest,
				new HttpRequestTokenHandler() {
					@Override
					public void handlePath(String path)
							throws HttpRequestTokeniseException {
						// Ignore path as only interested in parameters
					}

					@Override
					public void handleHttpParameter(String name, String value)
							throws HttpRequestTokeniseException {
						// Load the value
						try {
							valueLoader.loadValue(name, value);
						} catch (Exception ex) {
							throw new HttpRequestTokeniseException(ex);
						}
					}

					@Override
					public void handleQueryString(String queryString)
							throws HttpRequestTokeniseException {
						// Ignore query string as only interested in parameters
					}

					@Override
					public void handleFragment(String fragment)
							throws HttpRequestTokeniseException {
						// Ignore fragment as only interested in parameters
					}
				});
	}

}