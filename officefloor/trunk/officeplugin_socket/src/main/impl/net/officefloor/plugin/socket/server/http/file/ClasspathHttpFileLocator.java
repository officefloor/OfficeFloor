/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Locates a {@link HttpFile} from a {@link ClassLoader}.
 *
 * @author Daniel Sagenschneider
 */
public class ClasspathHttpFileLocator implements HttpFileLocator {

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
	 */
	public ClasspathHttpFileLocator(ClassLoader classLoader,
			String classpathPrefix, String defaultDirectoryFileName) {
		this.classLoader = classLoader;
		this.defaultDirectoryFileName = defaultDirectoryFileName;

		// Initiate prefix ready for use (trim and no trailing '/')
		classpathPrefix = classpathPrefix.trim();
		this.classpathPrefix = (classpathPrefix.endsWith("/") ? classpathPrefix
				.substring(0, (classpathPrefix.length() - 1)) : classpathPrefix);
	}

	/*
	 * ===================== HttpFileLocator ===============================
	 */

	@Override
	public HttpFile locateHttpFile(String path) throws IOException {

		// Transform to canonical path
		String canonicalPath = HttpFileUtil.transformToCanonicalPath(path);
		if (canonicalPath == null) {
			// Invalid path, so can not locate file
			return null;
		}

		// Determine if a directory
		int fileNameBegin = canonicalPath.lastIndexOf('/');
		String fileName = (fileNameBegin < 0 ? canonicalPath : canonicalPath
				.substring(fileNameBegin));
		if (fileName.indexOf('.') < 0) {
			// Directory (as no extension), so add default file name
			String separator = canonicalPath.endsWith("/") ? "" : "/";
			canonicalPath = canonicalPath + separator
					+ this.defaultDirectoryFileName;
		}

		// Create the path to locate the file (canonical always starts with '/')
		String resourcePath = this.classpathPrefix + canonicalPath;

		// Attempt to obtain the file
		InputStream inputStream = this.classLoader
				.getResourceAsStream(resourcePath);
		if (inputStream == null) {
			// Can not locate the file
			return null;
		}

		// Obtain the contents of the file
		ByteArrayOutputStream contents = new ByteArrayOutputStream();
		for (int value = inputStream.read(); value != -1; value = inputStream
				.read()) {
			contents.write(value);
		}
		inputStream.close();

		// Create the HTTP File
		HttpFile httpFile = new HttpFileImpl(canonicalPath, null, null,
				ByteBuffer.wrap(contents.toByteArray()).asReadOnlyBuffer());

		// Return the HTTP File
		return httpFile;
	}
}