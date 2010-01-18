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

package net.officefloor.plugin.socket.server.http.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Locates a {@link HttpFile} from a {@link ClassLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileFactory implements HttpFileFactory {

	/**
	 * Convenience method to create a {@link HttpFile} for the class path
	 * resource path.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader} to source the resource.
	 * @param resourcePath
	 *            Resource path.
	 * @return {@link HttpFile}.
	 * @throws InvalidHttpRequestUriException
	 *             Should the resource path be invalid.
	 * @throws IOException
	 *             If fails to source the resource.
	 */
	public static HttpFile createHttpFile(ClassLoader classLoader,
			String resourcePath) throws InvalidHttpRequestUriException,
			IOException {

		// Obtain the prefix as the first file segment. This allows it to be
		// stripped off and added again as per logic of creating HTTP file.
		String resourcePathPrefix = "";
		int separatorIndex = resourcePath.indexOf('/');
		if (separatorIndex > 0) {
			resourcePathPrefix = resourcePath.substring(0, separatorIndex);
		}

		// Create the HTTP file factory
		HttpFileFactory httpFileFactory = new ClasspathHttpFileFactory(
				classLoader, resourcePathPrefix, "index.html");
		final HttpFile httpFile = httpFileFactory.createHttpFile(null,
				resourcePath);

		// Return the HTTP file
		return httpFile;
	}

	/**
	 * {@link ClassLoader} to load the resource for the {@link HttpFile}.
	 */
	private final ClassLoader classLoader;

	/**
	 * <p>
	 * Prefix on the path to locate the {@link HttpFile}.
	 * <p>
	 * This restricts the access to files only contained within the prefix
	 * package (and its sub packages). Necessary so as not to expose all
	 * contents of the application (such as <code>*.class</code> files).
	 */
	private final String classpathPrefix;

	/**
	 * Name of file within the directory to default to use if the directory is
	 * specified.
	 */
	private final String defaultDirectoryFileName;

	/**
	 * Extension of the {@link #defaultDirectoryFileName}.
	 */
	private final String defaultDirectoryFileExtension;

	/**
	 * Listing of {@link HttpFileDescriber} instances to describe the located
	 * {@link HttpFile} instances.
	 */
	private final List<HttpFileDescriber> describers = new LinkedList<HttpFileDescriber>();

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader} to load the resource for the
	 *            {@link HttpFile}.
	 * @param classpathPrefix
	 *            Prefix on the path to locate the {@link HttpFile}.
	 * @param defaultDirectoryFileName
	 *            Name of file within the directory to default to use if the
	 *            directory is specified.
	 * @throws IllegalArgumentException
	 *             If the default directory file name does not have an
	 *             extension.
	 */
	public ClasspathHttpFileFactory(ClassLoader classLoader,
			String classpathPrefix, String defaultDirectoryFileName)
			throws IllegalArgumentException {
		this.classLoader = classLoader;
		this.defaultDirectoryFileName = defaultDirectoryFileName;

		// Obtain the default directory file extension
		int extensionBegin = this.defaultDirectoryFileName.lastIndexOf('.');
		if (extensionBegin < 0) {
			throw new IllegalArgumentException(
					"Default directory file name must have an extension");
		}
		this.defaultDirectoryFileExtension = this.defaultDirectoryFileName
				.substring(extensionBegin + 1); // +1 to not include '.'

		// Initiate prefix for use (trim, resource path and no trailing '/')
		classpathPrefix = classpathPrefix.trim().replace('.', '/');
		this.classpathPrefix = (classpathPrefix.endsWith("/") ? classpathPrefix
				.substring(0, (classpathPrefix.length() - 1)) : classpathPrefix);
	}

	/*
	 * ===================== HttpFileFactory ===============================
	 */

	@Override
	public void addHttpFileDescriber(HttpFileDescriber httpFileDescriber) {
		this.describers.add(httpFileDescriber);
	}

	@Override
	public HttpFile createHttpFile(File contextDirectory,
			String requestUriPath, HttpFileDescriber... httpFileDescribers)
			throws IOException, InvalidHttpRequestUriException {

		// Transform to canonical path
		String canonicalPath = HttpFileUtil
				.transformToCanonicalPath(requestUriPath);
		if (canonicalPath == null) {
			// Invalid path, so can not locate file
			return null;
		}

		// Obtain the file extension (or if none consider it a directory)
		String extension;
		int fileNameBegin = canonicalPath.lastIndexOf('/');
		String fileName = (fileNameBegin < 0 ? canonicalPath : canonicalPath
				.substring(fileNameBegin));
		int extensionBegin = fileName.lastIndexOf('.');
		if (extensionBegin < 0) {
			// Directory (as no extension), so use default file name
			String separator = canonicalPath.endsWith("/") ? "" : "/";
			canonicalPath = canonicalPath + separator
					+ this.defaultDirectoryFileName;
			extension = this.defaultDirectoryFileExtension;
		} else {
			// File, so obtain the extension (+1 to not include '.')
			extension = fileName.substring(extensionBegin + 1);
		}

		// Create the path to locate the file (canonical always starts with '/')
		String resourcePath = this.classpathPrefix + canonicalPath;

		// Attempt to obtain the file
		InputStream inputStream = this.classLoader
				.getResourceAsStream(resourcePath);
		if (inputStream == null) {
			// Can not locate the file, return not existing file
			return new HttpFileImpl(canonicalPath);
		}

		// Obtain the contents of the file
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		for (int value = inputStream.read(); value != -1; value = inputStream
				.read()) {
			data.write(value);
		}
		inputStream.close();
		ByteBuffer contents = ByteBuffer.wrap(data.toByteArray())
				.asReadOnlyBuffer();

		// Describe the file (duplicate contents to not alter markers)
		HttpFileDescriptionImpl description = new HttpFileDescriptionImpl(
				extension, contents.duplicate());
		boolean isDescribed = false;
		if (httpFileDescribers != null) {
			DESCRIBED: for (HttpFileDescriber describer : httpFileDescribers) {
				describer.describe(description);
				if (description.isDescribed()) {
					isDescribed = true;
					break DESCRIBED; // have description
				}
			}
		}
		if (!isDescribed) {
			// Not yet described so try with added describers
			DESCRIBED: for (HttpFileDescriber describer : this.describers) {
				describer.describe(description);
				if (description.isDescribed()) {
					break DESCRIBED; // have description
				}
			}
		}

		// Obtain the content description
		String contentEncoding = (description.contentEncoding == null ? ""
				: description.contentEncoding);
		String contentType = (description.contentType == null ? ""
				: description.contentType);
		Charset charset = description.charset;

		// Create the HTTP File
		HttpFile httpFile = new HttpFileImpl(canonicalPath, contentEncoding,
				contentType, charset, contents);

		// Return the HTTP File
		return httpFile;
	}

	/**
	 * {@link HttpFileDescription} implementation.
	 */
	private static class HttpFileDescriptionImpl implements HttpFileDescription {

		/**
		 * {@link HttpFile} extension.
		 */
		private final String extension;

		/**
		 * Contents of the {@link HttpFile}.
		 */
		private final ByteBuffer contents;

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
		 * @param extension
		 *            {@link HttpFile} extension.
		 * @param contents
		 *            Contents of the {@link HttpFile}.
		 */
		public HttpFileDescriptionImpl(String extension, ByteBuffer contents) {
			this.extension = extension;
			this.contents = contents;
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
		public String getExtension() {
			return this.extension;
		}

		@Override
		public ByteBuffer getContents() {
			return this.contents;
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
	}

}