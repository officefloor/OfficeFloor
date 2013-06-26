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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.web.http.resource.classpath.ClassPathHttpResourceNode;
import net.officefloor.plugin.web.http.resource.classpath.ClasspathHttpResourceFactory;

/**
 * {@link ResourceLocator} for the class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathResourceLocator implements ResourceLocator {

	/**
	 * {@link ClasspathHttpResourceFactory}.
	 */
	private final ClasspathHttpResourceFactory factory;

	/**
	 * {@link ClassLoader} to use to locate resources.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param classPathPrefix
	 *            Class path prefix.
	 * @param classLoader
	 *            {@link ClassLoader} to use to locate resources.
	 * @param defaultDirectoryFileNames
	 *            Names of the default files within a directory.
	 */
	public ClassPathResourceLocator(String classPathPrefix,
			ClassLoader classLoader, String... defaultDirectoryFileNames) {
		this.factory = ClasspathHttpResourceFactory.getHttpResourceFactory(
				classPathPrefix, classLoader, defaultDirectoryFileNames);
		this.classLoader = classLoader;
	}

	/*
	 * ================ ResourceLocator ===========================
	 */

	@Override
	public Set<String> getResourceChildren(String resourcePath) {

		// Obtain the node at the resource path
		ClassPathHttpResourceNode node = this.factory.getNode(resourcePath);
		if (node == null) {
			return Collections.emptySet(); // no resource, so no children
		}

		// Create the set of children
		ClassPathHttpResourceNode[] childNodes = node.getChildren();
		Set<String> children = new HashSet<String>(childNodes.length);
		for (ClassPathHttpResourceNode childNode : childNodes) {
			children.add(childNode.getNodePath());
		}

		// Return the children
		return children;
	}

	@Override
	public URL getResource(String resourcePath) throws MalformedURLException {

		// Obtain the node at the resource path
		ClassPathHttpResourceNode node = this.factory.getNode(resourcePath);
		if (node == null) {
			return null; // no resource at the path
		}

		// Return the input stream for the resource
		return this.classLoader.getResource(node.getClassPath());
	}

	@Override
	public InputStream getResourceAsStream(String resourcePath) {

		// Obtain the node at the resource path
		ClassPathHttpResourceNode node = this.factory.getNode(resourcePath);
		if (node == null) {
			return null; // no resource at the path
		}

		// Return the input stream for the resource
		return this.classLoader.getResourceAsStream(node.getClassPath());
	}

}