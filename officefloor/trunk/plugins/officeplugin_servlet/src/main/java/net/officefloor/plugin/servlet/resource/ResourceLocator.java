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
package net.officefloor.plugin.servlet.resource;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Locator of resources.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceLocator {

	/**
	 * Obtains the direct children for the resource.
	 * 
	 * @param resourcePath
	 *            Path to the resource.
	 * @return Direct children for the resource.
	 */
	Set<String> getResourceChildren(String resourcePath);

	/**
	 * Obtains the {@link URL} for the resource.
	 * 
	 * @param resourcePath
	 *            Path to the resource.
	 * @return {@link URL} to the resource or <code>null</code> if no resource
	 *         at path.
	 * @throws MalformedURLException
	 *             If resource path is malformed.
	 */
	URL getResource(String resourcePath) throws MalformedURLException;

	/**
	 * Obtains the {@link InputStream} to the contents of the resource.
	 * 
	 * @param resourcePath
	 *            Path to the resource.
	 * @return {@link InputStream} to the contents of the resource or
	 *         <code>null</code> if no resource at path.
	 */
	InputStream getResourceAsStream(String resourcePath);

}