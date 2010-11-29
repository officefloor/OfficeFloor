/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.security.scheme;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceUnknownPropertyError;

/**
 * Context for the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySourceContext<D extends Enum<D>> {

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws ManagedObjectSourceUnknownPropertyError
	 *             If property was not configured. Let this propagate as the
	 *             framework will handle it.
	 */
	String getProperty(String name)
			throws ManagedObjectSourceUnknownPropertyError;

	/**
	 * Obtains the property value or subsequently the default value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value if property not specified.
	 * @return Value of the property or the the default value if not specified.
	 */
	String getProperty(String name, String defaultValue);

	/**
	 * Flags that the {@link HttpSecuritySource} requires a dependency.
	 * 
	 * @param key
	 *            Key allowing {@link HttpSecuritySource} to identify the
	 *            dependency.
	 * @param dependencyType
	 *            Type expected for the dependency.
	 */
	void requireDependency(D key, Class<?> dependencyType);

}