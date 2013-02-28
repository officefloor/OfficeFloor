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
package net.officefloor.plugin.web.http.resource.classpath;

import net.officefloor.plugin.web.http.resource.AbstractHttpResource;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;

/**
 * Classpath {@link HttpDirectory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpDirectory extends AbstractHttpResource implements
		HttpDirectory {

	/**
	 * Class path prefix.
	 */
	private final String classPathPrefix;

	/**
	 * Names of the default {@link HttpFile} instances in order of searching for
	 * the default {@link HttpFile}.
	 */
	private final String[] defaultFileNames;

	/**
	 * Initiate.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @param classPathPrefix
	 *            Class path prefix.
	 * @param defaultFileNames
	 *            Names of the default {@link HttpFile} instances in order of
	 *            searching for the default {@link HttpFile}.
	 */
	public ClasspathHttpDirectory(String resourcePath, String classPathPrefix,
			String... defaultFileNames) {
		super(resourcePath);
		this.classPathPrefix = classPathPrefix;
		this.defaultFileNames = defaultFileNames;
	}

	/**
	 * Obtains the {@link ClasspathHttpResourceFactory}.
	 * 
	 * @return {@link ClasspathHttpResourceFactory}.
	 */
	private ClasspathHttpResourceFactory getFactory() {
		return ClasspathHttpResourceFactory.getHttpResourceFactory(
				this.classPathPrefix, null);
	}

	/*
	 * ==================== HttpDirectory =======================
	 */

	@Override
	public boolean isExist() {
		// Directory always exists
		return true;
	}

	@Override
	public HttpFile getDefaultFile() {

		// Obtain the factory
		ClasspathHttpResourceFactory factory = this.getFactory();

		// Obtain the node for this directory
		ClassPathHttpResourceNode node = factory.getNode(this.getPath());
		if (node == null) {
			return null; // no node, so no default file
		}

		// Find the first default file
		for (String defaultFileName : this.defaultFileNames) {

			// Obtain the node
			ClassPathHttpResourceNode child = node.getChild(defaultFileName);
			if (child == null) {
				continue; // attempt next default file
			}

			// Ensure is a file
			if (child.isDirectory()) {
				continue; // looking for file
			}

			// Found default file node so return its HTTP file
			return (HttpFile) factory.createHttpResource(child);
		}

		// As here no default file
		return null;
	}

	@Override
	public HttpResource[] listResources() {

		// Obtain the factory
		ClasspathHttpResourceFactory factory = this.getFactory();

		// Obtain the node for this directory
		ClassPathHttpResourceNode node = factory.getNode(this.getPath());
		if (node == null) {
			return new HttpResource[0]; // no node, so children
		}

		// Obtain the child nodes
		ClassPathHttpResourceNode[] children = node.getChildren();

		// Create the listing of resources
		HttpResource[] resources = new HttpResource[children.length];
		for (int i = 0; i < resources.length; i++) {
			resources[i] = factory.createHttpResource(children[i]);
		}

		// Return the listing of resources
		return resources;
	}

	/*
	 * =========================== Object =================================
	 */

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append(this.getClass().getSimpleName());
		text.append(": ");
		text.append(this.getPath());
		text.append(" (Class path prefix: ");
		text.append(this.classPathPrefix);
		text.append(")");
		return text.toString();
	}

}