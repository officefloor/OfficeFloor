/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;

/**
 * Locates a {@link HttpFile} from a {@link ClassLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathResourceSystem implements ResourceSystem {

	/**
	 * Root {@link ClassPathNode}.
	 */
	private final ClassPathNode root;

	/**
	 * Class path prefix for this {@link HttpResourceStore}.
	 */
	private final String classPathPrefix;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link ResourceSystemContext}.
	 */
	private final ResourceSystemContext context;

	/**
	 * {@link Path} to cache directory. Directory contents are not interrogated, so
	 * can re-use the directory.
	 */
	private final Path cacheDirectory;

	/**
	 * Instantiate.
	 * 
	 * @param context     {@link ResourceSystemContext}.
	 * @param classLoader {@link ClassLoader}.
	 * @throws IOException If fails to initiate resources from the class path.
	 */
	public ClasspathResourceSystem(ResourceSystemContext context, ClassLoader classLoader) throws IOException {
		this.context = context;
		this.classLoader = classLoader;

		// Ensure not absolute class path prefix
		String location = context.getLocation();
		if (location.startsWith("/")) {
			location = location.substring("/".length());
		}
		this.classPathPrefix = location;

		// Create the class path HTTP resource tree
		this.root = ClassPathNode.createClassPathResourceTree(this.classPathPrefix);

		// Obtain the cache directory
		this.cacheDirectory = context.createDirectory("/");
	}

	/**
	 * Obtains the {@link ClassPathNode} for resource path.
	 * 
	 * @param path Resource path.
	 * @return {@link ClassPathNode} for resource path or <code>null</code> if no
	 *         resource.
	 */
	public ClassPathNode getNode(String path) {

		// Search tree for node
		ClassPathNode node = this.root;
		int startIndex = 0;
		for (int i = 0; i < path.length(); i++) {
			char character = path.charAt(i);

			// If separator or last node entry
			if ((character == '/') || (i == (path.length() - 1))) {

				// Determine to include last character
				int includeLastCharacter = 0;
				if (character != '/') {
					// Must be last character, so include
					includeLastCharacter = 1;
				}

				// Obtain the node path (include last character if necessary)
				String nodePath = path.substring(startIndex, i + includeLastCharacter);

				// Obtain the child node (if have node path)
				if (nodePath.length() > 0) {
					node = node.getChild(nodePath);
					if (node == null) {
						// Not existing resource
						return null;
					}
				}

				// Increment start for next node (+1 to move past '/')
				startIndex = i + 1;
			}
		}

		// Return the node for the resource
		return node;
	}

	/*
	 * ===================== HttpResourceFactory ===============================
	 */

	@Override
	public Path getResource(String path) throws IOException {

		// Determine if have resources
		if (root.getChildren().length == 0) {
			/*
			 * Nothing found, so fall back to class loader.
			 * 
			 * Typically, this is due to running within container that creates its own
			 * ClassLoader. For example, Servlet container. Therefore, need to fallback to
			 * just using the ClassLoader, as can not scan class path.
			 */

			// Obtain the class path
			String classPath = this.root.getClassPath();
			if (path.startsWith("/")) {
				if (path.length() > 1) {
					// Append non-root path
					classPath += path;
				}
			} else {
				// Append path with separator
				classPath += "/" + path;
			}

			/*
			 * Determine if directory
			 * 
			 * Some class loaders will return the listing of resources in the directory,
			 * while others will return null. Therefore, need to check if default resources
			 * exist rather than basing on directory class path results.
			 */
			for (String defaultResourceName : this.context.getDirectoryDefaultResourceNames()) {

				// Determine if default resource exists
				String defaultResourcePath = classPath
						+ (defaultResourceName.startsWith("/") ? defaultResourceName : "/" + defaultResourceName);
				URL defaultResourceUrl = this.classLoader.getResource(defaultResourcePath);
				if (defaultResourceUrl != null) {
					// Directory resource
					return this.cacheDirectory;
				}
			}

			// Not directory, so determine if file
			return this.createFile(classPath, path);
		}

		// Obtains the node for the path
		ClassPathNode node = this.getNode(path);
		if (node == null) {
			// Not existing resource
			return null;
		}

		// Determine if directory (and return a directory)
		if (node.isDirectory()) {
			return this.cacheDirectory;
		}

		// Create and return the file
		String classPath = node.getClassPath();
		return this.createFile(classPath, path);
	}

	/**
	 * Creates the {@link Path} to the file.
	 * 
	 * @param classPath Class path to the file.
	 * @param path      Path requested.
	 * @return {@link Path} to the file or <code>null</code> if can not find file.
	 * @throws IOException If fails to create the file.
	 */
	private Path createFile(String classPath, String path) throws IOException {

		// Obtain the contents of the file
		InputStream contents = this.classLoader.getResourceAsStream(classPath);
		if (contents == null) {
			return null;
		}

		// Create file for resource
		Path file = this.context.createFile(path);
		Files.copy(contents, file, StandardCopyOption.REPLACE_EXISTING);

		// Return the created file
		return file;
	}

}
