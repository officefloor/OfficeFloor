/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.servlet.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Factory for creation of WAR aware {@link ClassLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ArchiveAwareClassLoaderFactory {

	/**
	 * {@link Path} instances to delete on shutdown.
	 */
	private static List<Path> pathsToDelete = new LinkedList<>();

	/**
	 * Registers {@link Path} to cleanup on shutdown.
	 * 
	 * @param path {@link Path} to cleanup on shutdown.
	 */
	private static void registerCleanupPath(Path path) {
		synchronized (pathsToDelete) {

			// Determine if first path to register clean up
			if (pathsToDelete.size() == 0) {
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					// Delete files then directories
					synchronized (pathsToDelete) {
						Collections.reverse(pathsToDelete);
						for (Path deletePath : pathsToDelete) {
							try {
								Files.deleteIfExists(deletePath);
							} catch (IOException ex) {
								// Best effort to delete
							}
						}
					}
				}, ArchiveAwareClassLoaderFactory.class.getSimpleName()));
			}

			// Add path for deletion
			pathsToDelete.add(path);
		}
	}

	/**
	 * Creates the {@link URLClassLoader}.
	 */
	private final Function<URL[], URLClassLoader> instantiator;

	/**
	 * Instantiate with specific parent {@link ClassLoader}.
	 * 
	 * @param parentClassLoader
	 */
	public ArchiveAwareClassLoaderFactory(ClassLoader parentClassLoader) {
		this.instantiator = (urls) -> new URLClassLoader(urls, parentClassLoader);
	}

	/**
	 * Creates the {@link ClassLoader}.
	 * 
	 * @param url           {@link URL} to archive {@link File}.
	 * @param classesPrefix Prefix of classes in archive.
	 * @param libPrefix     Prefix for libs in archive.
	 * @return {@link ClassLoader}.
	 * @throws IOException        If fails to create {@link ClassLoader}.
	 * @throws URISyntaxException If fails on {@link URL} for {@link ClassLoader}.
	 */
	public ClassLoader createClassLoader(URL url, String classesPrefix, String libPrefix)
			throws IOException, URISyntaxException {

		// Create the listing of URLs
		List<URL> classPathUrls = new ArrayList<URL>();

		// Create functions to support extracting jars
		Path[] memorizedTempDirectory = new Path[] { null };
		TempDirSupplier tempDir = () -> {
			if (memorizedTempDirectory[0] == null) {
				String urlPath = url.getPath();
				int fileNameIndex = urlPath.lastIndexOf('/');
				String fileName = urlPath.substring(fileNameIndex >= 0 ? fileNameIndex + "/".length() : 0);
				Path dir = Files.createTempDirectory(fileName.replace('.', '_'));
				registerCleanupPath(dir);
				memorizedTempDirectory[0] = dir;
			}
			return memorizedTempDirectory[0];
		};
		TempFileSupplier tempFile = (fileName) -> {
			Path dir = tempDir.createTempDir();
			Path file = Files.createTempFile(dir, "of_", "_" + fileName.replace('/', '_'));
			registerCleanupPath(file);
			return file;
		};

		// Decompose the WAR file into parts to enable URL class loader to work
		try (ZipFile war = new ZipFile(new File(url.toURI()))) {
			ZipOutputStream classesJar = null;
			try {
				Enumeration<? extends ZipEntry> entries = war.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String entryName = entry.getName();
					if (entryName.startsWith(libPrefix) && (entryName.toLowerCase().endsWith(".jar"))) {

						// Load library entry
						String libEntryName = entryName.substring(libPrefix.length());
						Path entryPath = tempFile.createTempFile(libEntryName);
						Files.copy(war.getInputStream(entry), entryPath, StandardCopyOption.REPLACE_EXISTING);
						classPathUrls.add(entryPath.toUri().toURL());

					} else if (entryName.startsWith(classesPrefix)) {

						// Load classes entry
						if (classesJar == null) {
							Path classesPath = tempFile.createTempFile("classes.jar");
							classesJar = new ZipOutputStream(new FileOutputStream(classesPath.toFile()));
							classPathUrls.add(classesPath.toUri().toURL());
						}
						String classesEntryName = entryName.substring(classesPrefix.length());
						if (classesEntryName.length() > 0) {

							// Copy in the class / resource
							ZipEntry classesEntry = new ZipEntry(classesEntryName);
							classesJar.putNextEntry(classesEntry);
							try (InputStream entryContents = war.getInputStream(entry)) {
								for (int value = entryContents.read(); value != -1; value = entryContents.read()) {
									classesJar.write(value);
								}
							}
							classesJar.closeEntry();
						}
					}
				}
			} finally {
				if (classesJar != null) {
					classesJar.close();
				}
			}
		}

		// Create the class loader
		return this.instantiator.apply(classPathUrls.toArray(new URL[classPathUrls.size()]));
	}

	/**
	 * Supplies a temporary directory.
	 */
	@FunctionalInterface
	private static interface TempDirSupplier {
		Path createTempDir() throws IOException;
	}

	/**
	 * Supplies a temporary file.
	 */
	@FunctionalInterface
	private static interface TempFileSupplier {
		Path createTempFile(String fileName) throws IOException;
	}
}
