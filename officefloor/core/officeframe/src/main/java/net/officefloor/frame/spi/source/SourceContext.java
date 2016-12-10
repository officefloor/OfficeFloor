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
package net.officefloor.frame.spi.source;

import java.io.InputStream;
import java.lang.reflect.Proxy;

/**
 * Generic context for a source.
 * 
 * @author Daniel Sagenschneider
 */
public interface SourceContext extends SourceProperties {

	/**
	 * <p>
	 * Indicates if just loading as a type.
	 * <p>
	 * When loading as a type the configuration provided is disregarded. This
	 * allows sources to know when to load singleton configuration that will
	 * take effect.
	 * <p>
	 * Whether this is <code>true</code> or <code>false</code> the resulting
	 * type should be the same.
	 * 
	 * @return <code>true</code> if loading as a type.
	 */
	boolean isLoadingType();

	/**
	 * Attempts to load the specified {@link Class}.
	 * 
	 * @param name
	 *            Name of the {@link Class}.
	 * @return {@link Class} or <code>null</code> if the {@link Class} can not
	 *         be found.
	 */
	Class<?> loadOptionalClass(String name);

	/**
	 * Loads the {@link Class}.
	 * 
	 * @param name
	 *            Name of the {@link Class}.
	 * @return {@link Class}.
	 * @throws UnknownClassError
	 *             If {@link Class} is not available. Let this propagate as
	 *             OfficeFloor will handle it.
	 */
	Class<?> loadClass(String name) throws UnknownClassError;

	/**
	 * Attempts to obtain the resource at the specified location.
	 * 
	 * @param location
	 *            Location of the resource.
	 * @return {@link InputStream} to the contents of the resource or
	 *         <code>null</code> if the resource can not be found.
	 */
	InputStream getOptionalResource(String location);

	/**
	 * Obtains the resource.
	 * 
	 * @param location
	 *            Location of the resource.
	 * @return {@link InputStream} to the contents of the resource.
	 * @throws UnknownResourceError
	 *             If resource is not found. Let this propagate as OfficeFloor
	 *             will handle it.
	 */
	InputStream getResource(String location) throws UnknownResourceError;

	/**
	 * <p>
	 * Obtains the {@link ClassLoader}.
	 * <p>
	 * This is only provided in specific cases where a {@link ClassLoader} is
	 * required (such as creating a {@link Proxy}). The other methods of this
	 * interface should be used in preference to the returned
	 * {@link ClassLoader}.
	 * 
	 * @return {@link ClassLoader}.
	 */
	ClassLoader getClassLoader();

}