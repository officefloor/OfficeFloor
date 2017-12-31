/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.stream;

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
import java.nio.file.attribute.PosixFilePermissions;

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
	 * Allow files to be read/written by executing user, and allow files to be
	 * read by group (useful for read-only debugging access).
	 */
	private static final FileAttribute<?>[] DIRECTORY_ATTRIBUTES = new FileAttribute[] {
			PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-----")) };

	/**
	 * Allow files to be read/written by executing user, and allow files to be
	 * read by group (useful for read-only debugging access).
	 */
	private static final FileAttribute<?>[] FILE_ATTRIBUTES = new FileAttribute[] {
			PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r-----")) };

	/**
	 * {@link CopyOption} values to create the temporary file.
	 */
	private static final CopyOption[] COPY_OPTIONS = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING };

	/**
	 * All working files, so delete on close and only allow read access to
	 * contents.
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
	 *            {@link Charset} to write the contents. May be
	 *            <code>null</code> to use default HTTP entity {@link Charset}.
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
	 * Creates a {@link FileChannel} to the temporary file using the default
	 * HTTP entity {@link Charset}.
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