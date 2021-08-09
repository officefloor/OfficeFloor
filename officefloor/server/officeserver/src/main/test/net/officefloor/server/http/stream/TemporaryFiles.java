/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

import net.officefloor.server.filesystem.OfficeFloorFileAttributes;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Provides means to manage temporary {@link FileChannel} content.
 * 
 * @author Daniel Sagenschneider
 */
public class TemporaryFiles {

	/**
	 * Default {@link TemporaryFiles}.
	 */
	private static TemporaryFiles defaultTemporaryFiles = null;

	/**
	 * Obtains the default {@link TemporaryFiles}.
	 * 
	 * @return Default {@link TemporaryFiles}.
	 * @throws IOException
	 *             If fails to create the default {@link TemporaryFiles}.
	 */
	public synchronized static TemporaryFiles getDefault() throws IOException {
		if (defaultTemporaryFiles == null) {
			defaultTemporaryFiles = new TemporaryFiles("default");
		}
		return defaultTemporaryFiles;
	}
	
	/**
	 * Allow files to be read/written by executing user, and allow files to be read
	 * by group (useful for read-only debugging access).
	 */
	private static final FileAttribute<?>[] DIRECTORY_ATTRIBUTES = OfficeFloorFileAttributes
			.getDefaultDirectoryAttributes();

	/**
	 * Allow files to be read/written by executing user, and allow files to be read
	 * by group (useful for read-only debugging access).
	 */
	private static final FileAttribute<?>[] FILE_ATTRIBUTES = OfficeFloorFileAttributes.getDefaultFileAttributes();

	/**
	 * {@link CopyOption} values to create the temporary file.
	 */
	private static final CopyOption[] COPY_OPTIONS = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING };

	/**
	 * All working files, so delete on close and only allow read access to contents.
	 */
	private static final OpenOption[] OPEN_OPTIONS = new OpenOption[] { StandardOpenOption.READ,
			StandardOpenOption.DELETE_ON_CLOSE };

	/**
	 * Directory to contain the temporary files.
	 */
	private final Path directory;

	/**
	 * Instantiate and creates the temporary files area.
	 * 
	 * @param prefix
	 *            Prefix for the temporary files area.
	 * @throws IOException
	 *             If fails to setup temporary files.
	 */
	public TemporaryFiles(String prefix) throws IOException {

		// Ensure have separator
		if ((prefix != null) && (!(prefix.endsWith("_")))) {
			prefix = prefix + "_";
		}

		// Create the temporary files area
		this.directory = Files.createTempDirectory(prefix, DIRECTORY_ATTRIBUTES);

		// Ensure the directory is deleted on exit
		this.directory.toFile().deleteOnExit();
	}

	/**
	 * Creates a {@link FileChannel} to the temporary file.
	 * 
	 * @param name
	 *            Name to aid identifying the temporary file on disk. May be
	 *            <code>null</code>.
	 * @param contents
	 *            {@link InputStream} to contents for the temporary file.
	 * @return {@link FileChannel} to the temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public FileChannel createTempFile(String name, InputStream contents) throws IOException {

		// Ensure have separator
		if ((name != null) && (!(name.startsWith("_")))) {
			name = "_" + name;
		}

		// Create the temporary file
		Path temporaryFile = Files.createTempFile(this.directory, null, name, FILE_ATTRIBUTES);

		// Write the contents to the file
		Files.copy(contents, temporaryFile, COPY_OPTIONS);

		// Open the file read-only and delete once done
		return FileChannel.open(temporaryFile, OPEN_OPTIONS);
	}

	/**
	 * Creates a {@link FileChannel} to a temporary file.
	 * 
	 * @param name
	 *            Name to aid identifying the temporary file on disk. May be
	 *            <code>null</code>.
	 * @param contents
	 *            Contents for the temporary file.
	 * @param offset
	 *            Offset into contents.
	 * @param length
	 *            Length from offset to write to file.
	 * @return {@link FileChannel} to the temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public FileChannel createTempFile(String name, byte[] contents, int offset, int length) throws IOException {
		return this.createTempFile(name, new ByteArrayInputStream(contents, offset, length));
	}

	/**
	 * Creates a {@link FileChannel} to a temporary file.
	 * 
	 * @param name
	 *            Name to aid identifying the temporary file on disk. May be
	 *            <code>null</code>.
	 * @param contents
	 *            Contents for the temporary file.
	 * @return {@link FileChannel} to the temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public FileChannel createTempFile(String name, byte[] contents) throws IOException {
		return this.createTempFile(name, contents, 0, contents.length);
	}

	/**
	 * Creates a {@link FileChannel} to the temporary file.
	 * 
	 * @param name
	 *            Name to aid identifying the temporary file on disk. May be
	 *            <code>null</code>.
	 * @param contents
	 *            Contents for the temporary file.
	 * @param charset
	 *            {@link Charset} to write the contents. May be <code>null</code> to
	 *            use default HTTP entity {@link Charset}.
	 * @return {@link FileChannel} to the temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public FileChannel createTempFile(String name, String contents, Charset charset) throws IOException {

		// Ensure have charset
		if (charset == null) {
			charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
		}

		// Obtain the bytes of content
		byte[] data = contents.getBytes(charset);

		// Create the temporary file
		return this.createTempFile(name, data);
	}

	/**
	 * Creates a {@link FileChannel} to the temporary file using the default HTTP
	 * entity {@link Charset}.
	 * 
	 * @param name
	 *            Name to aid identifying the temporary file on disk. May be
	 *            <code>null</code>.
	 * @param contents
	 *            Contents for the temporary file.
	 * @return {@link FileChannel} to the temporary file.
	 * @throws IOException
	 *             If fails to create temporary file.
	 */
	public FileChannel createTempFile(String name, String contents) throws IOException {
		return this.createTempFile(name, contents, null);
	}

}
