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
package net.officefloor.building.classpath;

import java.io.File;
import java.io.FileNotFoundException;

import net.officefloor.building.OfficeBuilding;

import org.apache.maven.embedder.MavenEmbedder;

/**
 * Factory for the creation of a {@link ClassPathBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathBuilderFactory {

	/**
	 * Obtains the User specific local repository directory.
	 * 
	 * @return User specific local repository directory.
	 */
	public static File getUserSpecificLocalRepositoryDirectory() {

		// Determine local repository from user configuration
		String localRepositoryPath = null;
		try {
			MavenEmbedder embedder = new MavenEmbedder();
			try {
				embedder.setClassLoader(ClassPathBuilderFactory.class
						.getClassLoader());
				embedder.setAlignWithUserInstallation(true);
				embedder.start();
				localRepositoryPath = embedder.getLocalRepository()
						.getBasedir();
			} finally {
				embedder.stop();
			}
		} catch (Throwable ex) {
			// Ignore and continue on with no user configured path
		}

		// Return if have local repository
		return (localRepositoryPath == null ? null : new File(
				localRepositoryPath));
	}

	/**
	 * <p>
	 * Obtains the default configured local repository directory.
	 * <p>
	 * Path is determined as follows:
	 * <ol>
	 * <li>{@link #getUserSpecificLocalRepositoryDirectory()}</li>
	 * <li><code>defaultPath</code></li>
	 * <li>temporary directory</li>
	 * </ol>
	 * <p>
	 * This method also ensures the directory is available by creating it if
	 * necessary.
	 * 
	 * @param defaultPath
	 *            Default path if not configured for user. May be
	 *            <code>null</code> to use temporary directory.
	 * @return Local repository directory.
	 */
	public static File getLocalRepositoryDirectory(String defaultPath)
			throws FileNotFoundException {

		// Attempt first with user specified
		File localRepositoryDirectory = getUserSpecificLocalRepositoryDirectory();
		if (localRepositoryDirectory != null) {
			ensureDirectoryExists(localRepositoryDirectory);
			return localRepositoryDirectory;
		}

		// Fall back to default path (if provided)
		if ((defaultPath != null) && (defaultPath.trim().length() > 0)) {
			localRepositoryDirectory = new File(defaultPath);
			ensureDirectoryExists(localRepositoryDirectory);
			return localRepositoryDirectory;
		}

		// No fall back path so use temporary directory
		localRepositoryDirectory = new File(System
				.getProperty("java.io.tmpdir"), OfficeBuilding.class
				.getSimpleName()
				+ "Repository");
		ensureDirectoryExists(localRepositoryDirectory);
		return localRepositoryDirectory;
	}

	/**
	 * Ensures the directory exists.
	 * 
	 * @param directory
	 *            Directory to ensure exists.
	 * @throws FileNotFoundException
	 *             If fails to ensure directory exists.
	 */
	private static void ensureDirectoryExists(File directory)
			throws FileNotFoundException {
		if (!directory.exists()) {
			// Ensure directory is available
			if (!directory.mkdirs()) {
				throw new FileNotFoundException(
						"Failed creating local repository "
								+ directory.getPath());
			}
		}
	}

	/**
	 * Local repository directory.
	 */
	private final File localRepositoryDirectory;

	/**
	 * Listing of the remote repository URLs.
	 */
	private final String[] remoteRepositoryUrls;

	/**
	 * Initiate.
	 * 
	 * @param defaultLocalRepositoryPath
	 *            Default local repository path should not be configured for
	 *            user. May be <code>null</code> to default to temporary
	 *            directory.
	 * @param remoteRepositoryUrls
	 *            Remote repository URLs.
	 * @throws FileNotFoundException
	 *             If can not find local repository.
	 */
	public ClassPathBuilderFactory(String defaultLocalRepositoryPath,
			String... remoteRepositoryUrls) throws FileNotFoundException {
		this.localRepositoryDirectory = getLocalRepositoryDirectory(defaultLocalRepositoryPath);
		this.remoteRepositoryUrls = remoteRepositoryUrls;
	}

	/**
	 * Creates a new {@link ClassPathBuilder}.
	 * 
	 * @return New {@link ClassPathBuilder}.
	 * @throws Exception
	 *             If fails to create a {@link ClassPathBuilder}.
	 */
	public ClassPathBuilder createClassPathBuilder() throws Exception {
		return new ClassPathBuilder(this.localRepositoryDirectory,
				this.remoteRepositoryUrls);
	}

}