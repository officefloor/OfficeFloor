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

package net.officefloor.web.resource.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.filesystem.OfficeFloorFileAttributes;
import net.officefloor.web.resource.spi.FileCache;
import net.officefloor.web.resource.spi.FileCacheFactory;
import net.officefloor.web.resource.spi.FileCacheService;

/**
 * Temporary directory {@link FileCacheService}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemporaryDirectoryFileCacheService implements FileCacheService, FileCacheFactory {

	/**
	 * Escapes the file path.
	 * 
	 * @param filePath File path.
	 * @return Escaped file path.
	 */
	private static String escapeFilePath(String filePath) {
		return filePath.replace('/', '_');
	}

	/**
	 * Directory {@link FileAttribute}.
	 */
	private static final FileAttribute<?>[] DIRECTORY_ATTRIBUTES = OfficeFloorFileAttributes
			.getDefaultDirectoryAttributes();

	/**
	 * File {@link FileAttribute}.
	 */
	private static final FileAttribute<?>[] FILE_ATTRIBUTES = OfficeFloorFileAttributes.getDefaultFileAttributes();

	/*
	 * =================== FileCacheService ========================
	 */

	@Override
	public FileCacheFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== FileCacheFactory ========================
	 */

	@Override
	public FileCache createFileCache(String name) throws IOException {
		return new TemporaryDirectoryFileCache(name);
	}

	/**
	 * Temporary directory {@link FileCache}.
	 */
	private class TemporaryDirectoryFileCache implements FileCache {

		/**
		 * Temporary directory {@link Path}.
		 */
		private final Path tempDirectory;

		/**
		 * Instantiate with the name of {@link FileCache}.
		 * 
		 * @param name Name of {@link FileCache}.
		 * @throws IOException If fails to create {@link FileCache}.
		 */
		public TemporaryDirectoryFileCache(String name) throws IOException {
			this.tempDirectory = Files.createTempDirectory(escapeFilePath(name) + "-", DIRECTORY_ATTRIBUTES);
		}

		/*
		 * ============== FileCache =========================
		 */

		@Override
		public Path createFile(String name) throws IOException {
			String suffix = escapeFilePath(name);
			Path file = Files.createTempFile(this.tempDirectory, null, "-" + suffix, DIRECTORY_ATTRIBUTES);
			return file;
		}

		@Override
		public Path createDirectory(String name) throws IOException {
			String prefix = escapeFilePath(name);
			Path directory = Files.createTempDirectory(this.tempDirectory, prefix + "-", FILE_ATTRIBUTES);
			return directory;
		}

		@Override
		public void close() throws IOException {
			Files.delete(this.tempDirectory);
		}
	}

}
