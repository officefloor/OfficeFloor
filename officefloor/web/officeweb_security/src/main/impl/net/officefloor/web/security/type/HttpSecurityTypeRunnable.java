/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link OfficeFloorCompilerRunnable} to load {@link HttpSecurityType}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("rawtypes")
public class HttpSecurityTypeRunnable implements OfficeFloorCompilerRunnable<HttpSecurityType> {

	/**
	 * Convenience method to load the {@link HttpSecurityType}.
	 * 
	 * @param httpSecuritySourceClassName {@link HttpSecuritySource} class name.
	 * @param properties                  {@link PropertyList}.
	 * @param compiler                    {@link OfficeFloorCompiler}.
	 * @return {@link HttpSecurityType}.
	 * @throws Exception If failure in loading type.
	 */
	public static HttpSecurityType<?, ?, ?, ?, ?> loadHttpSecurityType(String httpSecuritySourceClassName,
			PropertyList properties, OfficeFloorCompiler compiler) throws Exception {

		// Create the list of parameters
		List<String> parameters = new LinkedList<String>();
		parameters.add(httpSecuritySourceClassName);
		for (Property property : properties) {
			parameters.add(property.getName());
			parameters.add(property.getValue());
		}

		// Load the HTTP Security type
		HttpSecurityType<?, ?, ?, ?, ?> type = compiler.run(HttpSecurityTypeRunnable.class,
				(Object[]) parameters.toArray(new String[parameters.size()]));

		// Return the HTTP Security type
		return type;
	}

	/*
	 * ================ OfficeFloorCompilerRunnable ================
	 */

	@Override
	public HttpSecurityType<?, ?, ?, ?, ?> run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {

		// First parameter is the HTTP Security Source
		String httpSecuritySourceClassName = (String) parameters[0];

		// Load the HTTP Security Loader
		HttpSecurityLoader httpSecurityLoader = new HttpSecurityLoaderImpl(compiler);

		// Instantiate the HTTP Security Source
		Class<?> httpSecuritySourceClass = compiler.getClassLoader().loadClass(httpSecuritySourceClassName);
		HttpSecuritySource<?, ?, ?, ?, ?> httpSecuritySource = (HttpSecuritySource<?, ?, ?, ?, ?>) httpSecuritySourceClass
				.getDeclaredConstructor().newInstance();

		// Obtain the properties (pair values after HTTP Security Source)
		PropertyList properties = compiler.createPropertyList();
		for (int i = 1; i < parameters.length; i += 2) {
			String name = (String) parameters[i];
			String value = (String) parameters[i + 1];
			properties.addProperty(name).setValue(value);
		}

		// Load the type
		HttpSecurityType type = httpSecurityLoader.loadHttpSecurityType(httpSecuritySource, properties);

		// Return the type
		return type;
	}
}
