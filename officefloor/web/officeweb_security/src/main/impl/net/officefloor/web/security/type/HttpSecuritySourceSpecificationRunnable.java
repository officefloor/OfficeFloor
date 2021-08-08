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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * {@link OfficeFloorCompilerRunnable} to obtain the specification for the
 * {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourceSpecificationRunnable implements OfficeFloorCompilerRunnable<PropertyList> {

	/**
	 * Convenience method to load the specification for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @param httpSecuritySourceClassName {@link HttpSecuritySource} class name.
	 * @param compiler                    {@link OfficeFloorCompiler}.
	 * @return {@link PropertyList}.
	 * @throws Exception If failure in loading sspecification.
	 */
	public static PropertyList loadSpecification(String httpSecuritySourceClassName, OfficeFloorCompiler compiler)
			throws Exception {
		return compiler.run(HttpSecuritySourceSpecificationRunnable.class, httpSecuritySourceClassName);
	}

	/*
	 * =========== OfficeFloorCompilerRunnable ===================
	 */

	@Override
	public PropertyList run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {

		// First parameter is the HTTP Security Source
		String httpSecuritySourceClassName = (String) parameters[0];

		// Load the HTTP Security Loader
		HttpSecurityLoader httpSecurityLoader = new HttpSecurityLoaderImpl(compiler);

		// Instantiate the HTTP Security Source
		Class<?> httpSecuritySourceClass = compiler.getClassLoader().loadClass(httpSecuritySourceClassName);
		HttpSecuritySource<?, ?, ?, ?, ?> httpSecuritySource = (HttpSecuritySource<?, ?, ?, ?, ?>) httpSecuritySourceClass
				.getDeclaredConstructor().newInstance();

		// Load the specification
		PropertyList properties = httpSecurityLoader.loadSpecification(httpSecuritySource);

		// Return the properties
		return properties;
	}

}
