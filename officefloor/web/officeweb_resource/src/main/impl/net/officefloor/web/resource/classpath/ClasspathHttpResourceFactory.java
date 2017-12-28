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
import java.util.HashMap;
import java.util.Map;

import net.officefloor.web.resource.AbstractHttpFileDescription;
import net.officefloor.web.resource.AbstractHttpResourceFactory;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpFileDescription;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceFactory;
import net.officefloor.web.resource.NotExistHttpResource;

/**
 * Locates a {@link HttpFile} from a {@link ClassLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpResourceFactory extends AbstractHttpResourceFactory {

	/**
	 * <p>
	 * {@link ClasspathHttpResourceFactory} instances by class path prefix.
	 * <p>
	 * Typically within an application there will only be one of these.
	 */
	private static final Map<String, ClasspathHttpResourceFactory> factories = new HashMap<String, ClasspathHttpResourceFactory>(
			1);

	/**
	 * Obtains the {@link ClasspathHttpResourceFactory} for the class path
	 * prefix.
	 * 
	 * @param classPathPrefix
	 *            Class path prefix.
	 * @param classLoader
	 *            {@link ClassLoader} to use to obtain the {@link HttpResource}
	 *            instances.
	 * @param defaultDirectoryFileNames
	 *            Default directory file names. Should the
	 *            {@link ClasspathHttpResourceFactory} already be created for
	 *            the class path prefix, these values are ignored.
	 * @return {@link ClasspathHttpResourceFactory} for the class path prefix.
	 */
	public static ClasspathHttpResourceFactory getHttpResourceFactory(
			String classPathPrefix, ClassLoader classLoader,
			String... defaultDirectoryFileNames) {

		// Initiate prefix (trim, make resource path and no trailing '/')
		classPathPrefix = classPathPrefix.trim().replace('.', '/');
		classPathPrefix = (classPathPrefix.endsWith("/") ? classPathPrefix
				.substring(0, (classPathPrefix.length() - 1)) : classPathPrefix);

		// Attempt to obtain existing factory
		ClasspathHttpResourceFactory factory;
		synchronized (factories) {
			factory = factories.get(classPathPrefix);
			if (factory == null) {

				// Ensure not looking up
				if (classLoader == null) {
					throw new IllegalStateException(
							"Looking up unknown "
									+ ClasspathHttpResourceFactory.class
											.getSimpleName() + " '"
									+ classPathPrefix + "'");
				}

				// Not exist, so create factory
				factory = new ClasspathHttpResourceFactory(classPathPrefix,
						classLoader, defaultDirectoryFileNames);

				// Register the factory for the class path prefix
				factories.put(classPathPrefix, factory);
			}
		}

		// Return the factory
		return factory;
	}

	/**
	 * Allows clearing the {@link HttpResourceFactory} instances.
	 */
	public static void clearHttpResourceFactories() {
		synchronized (factories) {
			factories.clear();
		}
	}

	/**
	 * Obtains the contents of the {@link HttpResource} for the class path.
	 * 
	 * @param classpathPrefix
	 *            Class path prefix to identify the {@link HttpResourceFactory}.
	 * @param classPath
	 *            Class path to the {@link HttpResource}.
	 * @return ByteBuffer containing the contents of the {@link HttpResource}.
	 */
	public static ByteBuffer getHttpResourceContents(String classpathPrefix,
			String classPath) {

		// Obtain the resource factory for the class path prefix
		ClasspathHttpResourceFactory factory = factories.get(classpathPrefix);
		if (factory == null) {
			// Must have factory for class path prefix
			throw new IllegalStateException("Looking up unknown "
					+ ClasspathHttpResourceFactory.class.getSimpleName() + " '"
					+ classpathPrefix + "'");
		}

		// Obtain the input stream to potential resource
		InputStream inputStream = factory.classLoader
				.getResourceAsStream(classPath);

		// Obtain the resource contents
		return getHttpResourceContents(inputStream, classPath);
	}

	/**
	 * Root {@link ClassPathHttpResourceNode}.
	 */
	private final ClassPathHttpResourceNode root;

	/**
	 * Class path prefix for this {@link HttpResourceFactory}.
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
	private ClasspathHttpResourceFactory(String classPathPrefix,
			ClassLoader classLoader, String... defaultDirectoryFileNames) {
		this.defaultDirectoryFileNames = defaultDirectoryFileNames;
		this.classLoader = classLoader;
		this.classPathPrefix = classPathPrefix;

		// Create the class path HTTP resource tree
		this.root = ClassPathHttpResourceNode
				.createClassPathResourceTree(this.classPathPrefix);
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
				String nodePath = resourcePath.substring(startIndex, i
						+ includeLastCharacter);

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
			return new ClasspathHttpDirectory(node.getResourcePath(),
					this.classPathPrefix, this.defaultDirectoryFileNames);
		}

		// As here, resource is a file so have file described
		HttpFileDescriptionImpl description = new HttpFileDescriptionImpl(
				node.getResourcePath(), node.getClassPath());
		this.describeFile(description);

		// Create the HTTP File
		HttpFile httpFile = new ClasspathHttpFile(node.getResourcePath(),
				node.getClassPath(), this.classPathPrefix, description);

		// Return the HTTP File
		return httpFile;
	}

	/*
	 * ===================== HttpResourceFactory ===============================
	 */

	@Override
	public HttpResource createHttpResource(String applicationCanonicalPath)
			throws IOException {

		// Obtains the node for the path
		ClassPathHttpResourceNode node = this.getNode(applicationCanonicalPath);
		if (node == null) {
			// Not existing resource
			return new NotExistHttpResource(applicationCanonicalPath);
		}

		// Return the created HTTP resource
		return this.createHttpResource(node);
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
			return getHttpResourceContents(
					ClasspathHttpResourceFactory.this.classPathPrefix,
					this.classPath);
		}
	}

}