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

package net.officefloor.plugin.socket.server.http.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Locates a {@link HttpFile} from a {@link ClassLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpResourceFactory implements HttpResourceFactory {

	/**
	 * Empty {@link ByteBuffer}.
	 */
	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0])
			.asReadOnlyBuffer();

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
	 * @param defaultDirectoryFileNames
	 *            Default directory file names. Should the
	 *            {@link ClasspathHttpResourceFactory} already be created for
	 *            the class path prefix, these values are ignored.
	 * @return {@link ClasspathHttpResourceFactory} for the class path prefix.
	 */
	public static ClasspathHttpResourceFactory getHttpResourceFactory(
			String classPathPrefix, String... defaultDirectoryFileNames) {

		// Initiate prefix (trim, make resource path and no trailing '/')
		classPathPrefix = classPathPrefix.trim().replace('.', '/');
		classPathPrefix = (classPathPrefix.endsWith("/") ? classPathPrefix
				.substring(0, (classPathPrefix.length() - 1)) : classPathPrefix);

		// Attempt to obtain existing factory
		ClasspathHttpResourceFactory factory;
		synchronized (factories) {
			factory = factories.get(classPathPrefix);
			if (factory == null) {

				// Not exist, so create factory
				factory = new ClasspathHttpResourceFactory(classPathPrefix,
						defaultDirectoryFileNames);

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
	 * @return Class path of the {@link HttpResource}.
	 */
	public static ByteBuffer getHttpResourceContents(String classPath) {

		// Attempt to obtain the file
		InputStream inputStream = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream(classPath);
		if (inputStream == null) {
			// Can not locate the file, return no content
			return EMPTY_BUFFER;
		}

		try {
			// Obtain the contents of the file
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			for (int value = inputStream.read(); value != -1; value = inputStream
					.read()) {
				data.write(value);
			}
			inputStream.close();

			// Return the contents of the file
			return ByteBuffer.wrap(data.toByteArray()).asReadOnlyBuffer();

		} catch (IOException ex) {
			// Failed obtaining contents, return no content
			return EMPTY_BUFFER;
		}
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
	 * Names of files within the directory for searching for the default file.
	 */
	private final String[] defaultDirectoryFileNames;

	/**
	 * Listing of {@link HttpFileDescriber} instances to describe the created
	 * {@link HttpFile} instances.
	 */
	private final List<HttpFileDescriber> describers = new LinkedList<HttpFileDescriber>();

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
	 * @param defaultDirectoryFileNames
	 *            Names of files within the directory for searching for the
	 *            default file. The search for the default file follows the
	 *            order provided - returning the first default file found.
	 */
	private ClasspathHttpResourceFactory(String classPathPrefix,
			String... defaultDirectoryFileNames) {
		this.defaultDirectoryFileNames = defaultDirectoryFileNames;
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
			return new HttpDirectoryImpl(node.getResourcePath(),
					this.classPathPrefix, this.defaultDirectoryFileNames);
		}

		// As here, resource is a file so have file described
		HttpFileDescriptionImpl description = new HttpFileDescriptionImpl(node
				.getResourcePath(), node.getClassPath(), true);
		DESCRIBED: for (HttpFileDescriber describer : this.describers) {
			describer.describe(description);
			if (description.isDescribed()) {
				break DESCRIBED; // have description
			}
		}

		// Obtain the file description
		String contentEncoding = (description.contentEncoding == null ? ""
				: description.contentEncoding);
		String contentType = (description.contentType == null ? ""
				: description.contentType);
		Charset charset = description.charset;

		// Create the HTTP File
		HttpFile httpFile = new HttpFileImpl(node.getResourcePath(), node
				.getClassPath(), contentEncoding, contentType, charset);

		// Return the HTTP File
		return httpFile;
	}

	/*
	 * ===================== HttpResourceFactory ===============================
	 */

	@Override
	public void addHttpFileDescriber(HttpFileDescriber httpFileDescriber) {
		this.describers.add(httpFileDescriber);
	}

	@Override
	public HttpResource createHttpResource(String requestUriPath)
			throws IOException, InvalidHttpRequestUriException {

		// Transform to canonical path
		String canonicalPath = HttpResourceUtil
				.transformToCanonicalPath(requestUriPath);

		// Obtains the node for the path
		ClassPathHttpResourceNode node = this.getNode(canonicalPath);
		if (node == null) {
			// Not existing resource
			return new NotExistHttpResource(canonicalPath);
		}

		// Return the created HTTP resource
		return this.createHttpResource(node);
	}

	/**
	 * {@link HttpFileDescription} implementation.
	 */
	private static class HttpFileDescriptionImpl implements
			HttpFileDescription, HttpResource {

		/**
		 * {@link HttpResource} path.
		 */
		private final String resourcePath;

		/**
		 * Class path.
		 */
		private final String classPath;

		/**
		 * Flags if the {@link HttpFile} exists.
		 */
		private final boolean isExist;

		/**
		 * <code>Content-Encoding</code> for the {@link HttpFile}.
		 */
		public String contentEncoding = null;

		/**
		 * <code>Content-Type</code> for the {@link HttpFile}.
		 */
		public String contentType = null;

		/**
		 * {@link Charset} for the {@link HttpFile}.
		 */
		public Charset charset = null;

		/**
		 * Initiate.
		 * 
		 * @param resourcePath
		 *            {@link HttpResource} path.
		 * @param classPath
		 *            Class path.
		 * @param isExist
		 *            Flags if exists.
		 */
		public HttpFileDescriptionImpl(String resourcePath, String classPath,
				boolean isExist) {
			this.resourcePath = resourcePath;
			this.classPath = classPath;
			this.isExist = isExist;
		}

		/**
		 * Indicates if the {@link HttpFile} is described.
		 * 
		 * @return <code>true</code> if the {@link HttpFile} is described.
		 */
		public boolean isDescribed() {
			// Describe if have encoding and type (charset optional)
			return ((this.contentEncoding != null) && (this.contentType != null));
		}

		/*
		 * ================== HttpFileDescription ============================
		 */

		@Override
		public HttpResource getResource() {
			return this;
		}

		@Override
		public ByteBuffer getContents() {
			// Always attempt to obtain contents for file
			return getHttpResourceContents(this.classPath);
		}

		@Override
		public void setContentEncoding(String encoding) {
			this.contentEncoding = encoding;
		}

		@Override
		public void setContentType(String type, Charset charset) {
			this.contentType = type;
			this.charset = charset;
		}

		/*
		 * ======================= HttpResource ========================
		 */

		@Override
		public String getPath() {
			return this.resourcePath;
		}

		@Override
		public boolean isExist() {
			return this.isExist;
		}
	}

}