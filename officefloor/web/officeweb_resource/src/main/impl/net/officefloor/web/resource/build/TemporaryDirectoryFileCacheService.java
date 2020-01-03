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
		 * @param name
		 *            Name of {@link FileCache}.
		 * @throws IOException
		 *             If fails to create {@link FileCache}.
		 */
		public TemporaryDirectoryFileCache(String name) throws IOException {
			this.tempDirectory = Files.createTempDirectory(name + "-", DIRECTORY_ATTRIBUTES);
		}

		/*
		 * ============== FileCache =========================
		 */

		@Override
		public Path createFile(String name) throws IOException {
			String suffix = name.replace('/', '_');
			Path file = Files.createTempFile(this.tempDirectory, null, "-" + suffix, DIRECTORY_ATTRIBUTES);
			return file;
		}

		@Override
		public Path createDirectory(String name) throws IOException {
			String prefix = name.replace('/', '_');
			Path directory = Files.createTempDirectory(this.tempDirectory, prefix + "-", FILE_ATTRIBUTES);
			return directory;
		}

		@Override
		public void close() throws IOException {
			Files.delete(this.tempDirectory);
		}
	}

}