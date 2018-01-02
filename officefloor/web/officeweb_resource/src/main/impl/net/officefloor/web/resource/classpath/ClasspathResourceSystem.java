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
package net.officefloor.web.resource.classpath;

import java.io.IOException;
import java.io.InputStream;
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
	 * Root {@link ClassPathHttpResourceNode}.
	 */
	private final ClassPathHttpResourceNode root;

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
	 * {@link Path} to cache directory. Directory contents are not interrogated,
	 * so can re-use the directory.
	 */
	private final Path cacheDirectory;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link ResourceSystemContext}.
	 * @throws IOException
	 *             If fails to initiate resources from the class path.
	 */
	public ClasspathResourceSystem(ResourceSystemContext context) throws IOException {
		this.classPathPrefix = context.getLocation();
		this.context = context;

		// Create the class path HTTP resource tree
		this.root = ClassPathHttpResourceNode.createClassPathResourceTree(this.classPathPrefix);

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		this.classLoader = classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();

		// Obtain the cache directory
		this.cacheDirectory = context.createDirectory("/");
	}

	/**
	 * Obtains the {@link ClassPathHttpResourceNode} for resource path.
	 * 
	 * @param path
	 *            Resource path.
	 * @return {@link ClassPathHttpResourceNode} for resource path or
	 *         <code>null</code> if no resource.
	 */
	public ClassPathHttpResourceNode getNode(String path) {

		// Search tree for node
		ClassPathHttpResourceNode node = this.root;
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

		// Obtains the node for the path
		ClassPathHttpResourceNode node = this.getNode(path);
		if (node == null) {
			// Not existing resource
			return null;
		}

		// Determine if directory (and return a directory)
		if (node.isDirectory()) {
			return this.cacheDirectory;
		}

		// Obtain the input stream to resource contents
		String classPath = node.getClassPath();
		InputStream contents = this.classLoader.getResourceAsStream(classPath);

		// Create file for resource
		Path file = this.context.createFile(path);
		Files.copy(contents, file, StandardCopyOption.REPLACE_EXISTING);

		// Return the created file
		return file;
	}

}