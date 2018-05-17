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
	 * @param httpSecuritySourceClassName
	 *            {@link HttpSecuritySource} class name.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @return {@link PropertyList}.
	 * @throws Exception
	 *             If failure in loading sspecification.
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
				.newInstance();

		// Load the specification
		PropertyList properties = httpSecurityLoader.loadSpecification(httpSecuritySource);

		// Return the properties
		return properties;
	}

}