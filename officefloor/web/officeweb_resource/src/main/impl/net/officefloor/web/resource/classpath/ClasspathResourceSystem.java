/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
	 * @param context {@link ResourceSystemContext}.
	 * @throws IOException If fails to initiate resources from the class path.
	 */
	public ClasspathResourceSystem(ResourceSystemContext context) throws IOException {
		this.classPathPrefix = context.getLocation();
		this.context = context;

		// Create the class path HTTP resource tree
		this.root = ClassPathNode.createClassPathResourceTree(this.classPathPrefix);

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		this.classLoader = classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();

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
			String classPath = this.root.getClassPath() + (path.startsWith("/") ? path : "/" + path);

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