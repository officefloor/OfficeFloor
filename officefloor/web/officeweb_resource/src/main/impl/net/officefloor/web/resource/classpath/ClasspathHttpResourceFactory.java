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
import java.nio.ByteBuffer;

import net.officefloor.web.resource.HttpDirectory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.build.HttpFileDescription;
import net.officefloor.web.resource.impl.AbstractHttpFileDescription;
import net.officefloor.web.resource.impl.AbstractHttpResourceFactory;
import net.officefloor.web.resource.impl.NotExistHttpResource;

/**
 * Locates a {@link HttpFile} from a {@link ClassLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpResourceFactory extends AbstractHttpResourceFactory {

	/**
	 * Obtains the contents of the {@link HttpResource} for the class path.
	 * 
	 * @param classPath
	 *            Class path to the {@link HttpResource}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return ByteBuffer containing the contents of the {@link HttpResource}.
	 */
	public static ByteBuffer getHttpResourceContents(String classPath, ClassLoader classLoader) {

		// Obtain the input stream to potential resource
		InputStream inputStream = classLoader.getResourceAsStream(classPath);

		// Obtain the resource contents
		return getHttpResourceContents(inputStream, classPath);
	}

	/**
	 * Root {@link ClassPathHttpResourceNode}.
	 */
	private final ClassPathHttpResourceNode root;

	/**
	 * Class path prefix for this {@link HttpResourceStore}.
	 */
	private final String classPathPrefix;

	/**
	 * {@link ClassLoader} to use to obtain the {@link HttpResource}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Names of files within the directory for searching for the default file.
	 */
	private final String[] defaultDirectoryFileNames;

	/**
	 * Obtain an instance via static method
	 * {@link #getHttpResourceFactory(String, String...)}.
	 * 
	 * @param classpathPrefix
	 *            Prefix on the path to locate the {@link HttpFile}. This
	 *            restricts the access to files only contained within the prefix
	 *            package (and its sub packages). Necessary so as not to expose
	 *            all contents of the application (such as <code>*.class</code>
	 *            files).
	 * @param classLoader
	 *            {@link ClassLoader} to use to obtain the {@link HttpResource}.
	 * @param defaultDirectoryFileNames
	 *            Names of files within the directory for searching for the
	 *            default file. The search for the default file follows the
	 *            order provided - returning the first default file found.
	 */
	private ClasspathHttpResourceFactory(String classPathPrefix, ClassLoader classLoader,
			String... defaultDirectoryFileNames) {
		this.defaultDirectoryFileNames = defaultDirectoryFileNames;
		this.classLoader = classLoader;
		this.classPathPrefix = classPathPrefix;

		// Create the class path HTTP resource tree
		this.root = ClassPathHttpResourceNode.createClassPathResourceTree(this.classPathPrefix);
	}

	/**
	 * Obtains the {@link ClassPathHttpResourceNode} for resource path.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @return {@link ClassPathHttpResourceNode} for resource path or
	 *         <code>null</code> if no resource.
	 */
	public ClassPathHttpResourceNode getNode(String resourcePath) {

		// Search tree for node
		ClassPathHttpResourceNode node = this.root;
		int startIndex = 0;
		for (int i = 0; i < resourcePath.length(); i++) {
			char character = resourcePath.charAt(i);

			// If separator or last node entry
			if ((character == '/') || (i == (resourcePath.length() - 1))) {

				// Determine to include last character
				int includeLastCharacter = 0;
				if (character != '/') {
					// Must be last character, so include
					includeLastCharacter = 1;
				}

				// Obtain the node path (include last character if necessary)
				String nodePath = resourcePath.substring(startIndex, i + includeLastCharacter);

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

	/**
	 * Creates the {@link HttpResource}.
	 * 
	 * @param node
	 *            {@link ClassPathHttpResourceNode} for the created
	 *            {@link HttpResource}.
	 * @return {@link HttpResource}.
	 */
	public HttpResource createHttpResource(ClassPathHttpResourceNode node) {

		// Determine if a directory
		if (node.isDirectory()) {
			// Directory, so create and return the directory
			return new ClasspathHttpDirectory(node.getResourcePath(), this.classPathPrefix,
					this.defaultDirectoryFileNames);
		}

		// As here, resource is a file so have file described
		HttpFileDescriptionImpl description = new HttpFileDescriptionImpl(node.getResourcePath(), node.getClassPath());
		this.describeFile(description);

		// Create the HTTP File
		HttpFile httpFile = new ClasspathHttpFile(node.getResourcePath(), node.getClassPath(), this.classPathPrefix,
				description);

		// Return the HTTP File
		return httpFile;
	}

	/*
	 * ===================== HttpResourceFactory ===============================
	 */

	@Override
	public HttpResource getHttpResource(String applicationCanonicalPath) throws IOException {

		// Obtains the node for the path
		ClassPathHttpResourceNode node = this.getNode(applicationCanonicalPath);
		if (node == null) {
			// Not existing resource
			return new NotExistHttpResource(applicationCanonicalPath);
		}

		// Return the created HTTP resource
		return this.createHttpResource(node);
	}

	@Override
	public HttpResource getDefaultHttpResource(HttpDirectory directory) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResource[] listHttpResources(HttpDirectory directory) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@link HttpFileDescription} implementation.
	 */
	private class HttpFileDescriptionImpl extends AbstractHttpFileDescription {

		/**
		 * Class path.
		 */
		private final String classPath;

		/**
		 * Initiate.
		 * 
		 * @param resourcePath
		 *            {@link HttpResource} path.
		 * @param classPath
		 *            Class path.
		 */
		public HttpFileDescriptionImpl(String resourcePath, String classPath) {
			super(resourcePath);
			this.classPath = classPath;
		}

		/*
		 * ================== HttpFileDescription ============================
		 */

		@Override
		public ByteBuffer getContents() {
			// Always attempt to obtain contents for file
			return getHttpResourceContents(this.classPath, ClasspathHttpResourceFactory.this.classLoader);
		}
	}

}