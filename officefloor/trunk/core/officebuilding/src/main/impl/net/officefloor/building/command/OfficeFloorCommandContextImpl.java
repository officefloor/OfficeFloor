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
package net.officefloor.building.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;
import net.officefloor.console.OfficeBuilding;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectSorter;

/**
 * {@link OfficeFloorCommandContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCommandContextImpl implements OfficeFloorCommandContext {

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
				embedder.setClassLoader(OfficeFloorCommand.class
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
	public static File getLocalRepositoryDirectory(File defaultPath)
			throws FileNotFoundException {

		// Attempt first with user specified
		File userLocalRepository = getUserSpecificLocalRepositoryDirectory();
		if (userLocalRepository != null) {
			ensureDirectoryExists(userLocalRepository);
			return userLocalRepository;
		}

		// Fall back to default path (if provided)
		if (defaultPath != null) {
			ensureDirectoryExists(defaultPath);
			return defaultPath;
		}

		// No fall back path so use temporary directory
		File tmplocalRepository = new File(
				System.getProperty("java.io.tmpdir"), OfficeBuilding.class
						.getSimpleName()
						+ "Repository");
		ensureDirectoryExists(tmplocalRepository);
		return tmplocalRepository;
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
		// Ensure directory is available
		if ((!directory.exists()) && (!directory.mkdirs())) {
			throw new FileNotFoundException("Failed creating local repository "
					+ directory.getPath());
		}
	}

	/**
	 * Obtains the artifact name.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Artifact version.
	 * @param type
	 *            Artifact version.
	 * @param classifier
	 *            Artifact classifier. May be <code>null</code>.
	 * @return Artifact name.
	 */
	private static String getArtifactName(String groupId, String artifactId,
			String version, String type, String classifier) {
		return ArtifactUtils.artifactId(groupId, artifactId, type, classifier,
				version);
	}

	/**
	 * {@link MavenEmbedder}.
	 */
	private final MavenEmbedder maven;

	/**
	 * Local {@link ArtifactRepository}.
	 */
	private final ArtifactRepository localRepository;

	/**
	 * Remote {@link ArtifactRepository} instances.
	 */
	private final List<ArtifactRepository> remoteRepositories;

	/**
	 * {@link OfficeFloorDecorator} instances.
	 */
	private final OfficeFloorDecorator[] decorators;

	/**
	 * Listing of class path entries in order for the realised class path.
	 */
	private final List<String> classPathEntries = new LinkedList<String>();

	/**
	 * Environment.
	 */
	private final Properties environment = new Properties();

	/**
	 * Warnings regarding building the class path.
	 */
	private final List<String> classPathWarnings = new LinkedList<String>();

	/**
	 * Initiate.
	 * 
	 * @param localRepositoryDirectory
	 *            Local {@link ArtifactRepository}.
	 * @param remoteRepositoryUrls
	 *            Remote {@link ArtifactRepository} instances.
	 * @param decorators
	 *            {@link OfficeFloorDecorator} instances.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public OfficeFloorCommandContextImpl(File localRepositoryDirectory,
			String[] remoteRepositoryUrls, OfficeFloorDecorator[] decorators)
			throws Exception {
		this.decorators = decorators;

		// Default local repository
		File defaultLocalRepository = getLocalRepositoryDirectory(localRepositoryDirectory);

		// Create the Maven Embedder
		this.maven = new MavenEmbedder();
		this.maven.setClassLoader(this.getClass().getClassLoader());
		this.maven.setLocalRepositoryDirectory(defaultLocalRepository);
		this.maven.start();
		try {

			// Obtain the local repository
			this.localRepository = this.maven.getLocalRepository();

			// Create the listing of remote repositories
			this.remoteRepositories = new ArrayList<ArtifactRepository>(
					remoteRepositoryUrls.length);
			for (int i = 0; i < remoteRepositoryUrls.length; i++) {
				ArtifactRepository remoteRepository = this.maven
						.createRepository(remoteRepositoryUrls[i],
								"RemoteRepository" + i);
				this.remoteRepositories.add(remoteRepository);
			}

		} catch (Exception ex) {
			// Ensure stopped
			this.maven.stop();
			throw ex;
		}
	}

	/**
	 * Obtains the {@link OfficeFloorCommand} class path.
	 * 
	 * @return {@link OfficeFloorCommand} class path.
	 */
	public String getCommandClassPath() {

		// Build the class path
		StringBuilder path = new StringBuilder();
		boolean isFirst = true;
		for (String entry : this.classPathEntries) {

			// Provide separator between entries
			if (!isFirst) {
				path.append(File.pathSeparator);
			}
			isFirst = false; // for next iteration

			// Add the class path entry
			path.append(entry);
		}

		// Return the class path
		return path.toString();
	}

	/**
	 * Obtains the {@link OfficeFloorCommand} environment.
	 * 
	 * @return {@link OfficeFloorCommand} environment.
	 */
	public Properties getCommandEnvironment() {
		return this.environment;
	}

	/**
	 * Obtains the warnings.
	 * 
	 * @return Warnings.
	 */
	public String[] getWarnings() {
		return this.classPathWarnings.toArray(new String[this.classPathWarnings
				.size()]);
	}

	/**
	 * Includes {@link Artifact} as class path entry.
	 * 
	 * @param artifact
	 *            {@link Artifact}.
	 */
	private void includeClassPathEntry(Artifact artifact) {

		// Obtain path to artifact
		String artifactPath = null;
		try {
			artifactPath = artifact.getFile().getCanonicalPath();
		} catch (IOException ex) {
			this.addClassPathWarning("Failed to get path for artifact "
					+ artifact.getId(), ex);
			return; // not include as failed to obtain path
		}

		// Include the artifact
		this.includeClassPathEntry(artifactPath);
	}

	/**
	 * Releases resources.
	 * 
	 * @throws Exception
	 *             if fails to release resources.
	 */
	public void close() throws Exception {
		this.maven.stop();
	}

	/**
	 * Adds a class path warning.
	 * 
	 * @param description
	 *            Description of the warning.
	 * @param cause
	 *            Cause of warning.
	 */
	private void addClassPathWarning(String description, Throwable cause) {
		String message = cause.getMessage();
		String causeMessage = ((message == null)
				|| (message.trim().length() == 0) ? cause.getClass().getName()
				: message + " [" + cause.getClass().getSimpleName() + "]");
		this.classPathWarnings.add(description + " (" + causeMessage + ")");
	}

	/**
	 * Obtains the {@link MavenProject} POM.
	 * 
	 * @param artifactName
	 *            Name of artifact for the POM. This is only used to identify
	 *            the artifact for warnings.
	 * @param pomFile
	 *            <code>pom.xml</code> {@link File}.
	 * @return {@link MavenProject} POM.
	 */
	private MavenProject getPom(String artifactName, File pomFile) {

		// Attempt to read in the POM
		MavenProject pom = null;
		try {
			// Read as Model
			Model model = this.maven.readModel(pomFile);

			// Ensure remove distribution status to allow loading
			DistributionManagement distribution = model
					.getDistributionManagement();
			if ((distribution != null) && (distribution.getStatus() != null)) {
				distribution.setStatus(null);
			}

			// Add the remote repositories to project to allow resolution
			for (ArtifactRepository remoteRepository : this.remoteRepositories) {
				Repository repository = new Repository();
				repository.setId(remoteRepository.getId());
				repository.setUrl(remoteRepository.getUrl());
				model.addRepository(repository);
			}

			// Create another POM file with changes
			pomFile = File.createTempFile(OfficeFloorCommand.class
					.getSimpleName(), "pom");
			FileWriter writer = new FileWriter(pomFile);
			this.maven.writeModel(writer, model);
			writer.close();

			// Read in the Maven Project
			pom = this.maven.readProjectWithDependencies(pomFile);

		} catch (Exception ex) {
			// Indicate warning that unable to obtain the POM
			this.addClassPathWarning("Failed to read in POM for artifact "
					+ artifactName, ex);
		}

		// Return the Maven Project POM
		return pom;
	}

	/**
	 * Obtains the {@link MavenProject} POM from the archive.
	 * 
	 * @param archiveFile
	 *            Archive {@link File}.
	 * @return {@link MavenProject} POM.
	 */
	private MavenProject getArchivePom(File archiveFile) {

		// Attempt to open archive file
		ZipFile archive = null;
		try {
			archive = new ZipFile(archiveFile, ZipFile.OPEN_READ);
		} catch (Exception ex) {
			return null; // Not archive
		}

		// Obtain the pom.xml file from within the archive
		ZipEntry pomEntry = null;
		for (Enumeration<? extends ZipEntry> iterator = archive.entries(); iterator
				.hasMoreElements();) {
			ZipEntry entry = iterator.nextElement();

			// Ensure pom.xml within the META-INF/maven location
			String name = entry.getName();
			if (name.startsWith("META-INF/maven/")
					&& (name.endsWith("/pom.xml"))) {
				// Found the pom
				pomEntry = entry;
				break; // entry found
			}
		}
		if (pomEntry == null) {
			return null; // no pom.xml
		}

		// Make the POM contents available as file for reading
		File pomFile = null;
		try {
			InputStream pomContents = archive.getInputStream(pomEntry);
			pomFile = File.createTempFile(OfficeBuilding.class.getSimpleName(),
					"pom");
			OutputStream pomOutput = new FileOutputStream(pomFile);
			for (int value = pomContents.read(); value != -1; value = pomContents
					.read()) {
				pomOutput.write(value);
			}
			pomOutput.close();
			pomContents.close();
		} catch (IOException ex) {
			// Failed to extract pom.xml file from archive
			this.addClassPathWarning("Failed to extract pom.xml from archive "
					+ archiveFile.getPath(), ex);
		}

		// Return the POM
		return this.getPom(archiveFile.getPath() + "#" + pomEntry.getName(),
				pomFile);
	}

	/**
	 * Retrieves the resolved {@link Artifact}.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version.
	 * @param type
	 *            Type.
	 * @param classifier
	 *            Classifier, may be <code>null</code>.
	 * @return Resolved {@link Artifact}.
	 */
	private Artifact retrieveResolvedArtifact(String groupId,
			String artifactId, String version, String type, String classifier) {

		// Attempt to resolve the artifact
		Artifact artifact = null;
		try {
			artifact = this.maven.createArtifactWithClassifier(groupId,
					artifactId, version, type, classifier);
			this.maven.resolve(artifact, this.remoteRepositories,
					this.localRepository);

		} catch (Exception ex) {
			// Failed to resolve artifact
			this.addClassPathWarning("Failed to resolve artifact "
					+ getArtifactName(groupId, artifactId, version, type,
							classifier), ex);
		}

		// Return the artifact (if resolved)
		return artifact;
	}

	/**
	 * Retrieves the resolved POM {@link MavenProject}.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version.
	 * @return Resolved POM {@link MavenProject}.
	 */
	private MavenProject retrieveResolvedPom(String groupId, String artifactId,
			String version) {

		// Obtain the POM file
		Artifact pomArtifact = this.retrieveResolvedArtifact(groupId,
				artifactId, version, "pom", null);
		if (pomArtifact == null) {
			return null; // POM not resolved
		}

		// Obtain the POM
		MavenProject pom = this.getPom(groupId + ":" + artifactId + ":"
				+ version + ":pom", pomArtifact.getFile());

		// Return the POM
		return pom;
	}

	/**
	 * Includes the dependencies identified in the POM.
	 * 
	 * @param artifactName
	 *            {@link Artifact} name.
	 * @param pom
	 *            {@link MavenProject} POM.
	 * @throws Exception
	 *             If fails to include the dependencies.
	 */
	@SuppressWarnings("unchecked")
	private void includePomDependencies(String artifactName, MavenProject pom) {

		// Obtain the artifacts
		List<Artifact> artifacts = (List<Artifact>) pom.getRuntimeArtifacts();

		// Load mapping of POM to artifact
		Map<MavenProject, Artifact> pomToArtifact = new HashMap<MavenProject, Artifact>();
		for (Artifact artifact : artifacts) {

			// Obtain details of artifact
			String groupId = artifact.getGroupId();
			String artifactId = artifact.getArtifactId();
			String version = artifact.getVersion();

			// Obtain the POM for the artifact
			MavenProject dependencyPom = this.retrieveResolvedPom(groupId,
					artifactId, version);
			if (dependencyPom == null) {
				continue; // not able to resolve POM
			}

			// Map the artifact by its POM
			pomToArtifact.put(dependencyPom, artifact);
		}

		// Sort the dependencies for correct class path order
		List<MavenProject> sortedPoms = null;
		try {
			sortedPoms = (List<MavenProject>) new ProjectSorter(
					new ArrayList<MavenProject>(pomToArtifact.keySet()))
					.getSortedProjects();
		} catch (Exception ex) {
			// Should be able to sort dependencies
			this.addClassPathWarning(
					"Failed to sort dependencies for artifact " + artifactName,
					ex);
			return; // dependencies require to be sorted
		}

		// Include the artifacts in sorted order
		for (MavenProject project : sortedPoms) {
			Artifact artifact = pomToArtifact.get(project);
			if (artifact != null) {
				// Artifact resolved and available so include
				this.includeClassPathEntry(artifact);
			}
		}
	}

	/*
	 * ======================= OfficeFloorCommandContext ======================
	 */

	@Override
	public void includeClassPathEntry(String classPathEntry) {

		// Create the decorator context
		DecoratorContext context = new DecoratorContext(classPathEntry);

		// Decorate for the class path entry
		for (OfficeFloorDecorator decorator : this.decorators) {
			try {
				decorator.decorate(context);
			} catch (Exception ex) {
				this.addClassPathWarning("Failed decoration by "
						+ decorator.getClass().getName()
						+ " for class path entry " + classPathEntry, ex);
			}
		}

		// Determine if class path entry overridden
		if (context.resolvedClassPathEntries.size() > 0) {
			// Include the overridden class path entries
			for (String resolvedClassPathEntry : context.resolvedClassPathEntries) {
				this.classPathEntries.add(resolvedClassPathEntry);
			}
		} else {
			// Not overridden so include class path entry
			this.classPathEntries.add(classPathEntry);
		}
	}

	@Override
	public void includeClassPathArtifact(String artifactLocation) {

		// Include the class path artifact
		this.includeClassPathEntry(artifactLocation);

		// Find the pom to include dependencies
		File artifactFile = new File(artifactLocation);
		if (artifactFile.isFile()) {

			// Attempt to obtain from archive (e.g. jar, war, etc)
			MavenProject pom = this.getArchivePom(artifactFile);
			if (pom == null) {
				return; // pom not found, so no dependencies
			}

			// Include the dependencies within POM
			this.includePomDependencies(artifactLocation, pom);
		}
	}

	@Override
	public void includeClassPathArtifact(String groupId, String artifactId,
			String version, String type, String classifier) {

		// Default the type
		type = (type == null ? "jar" : type);

		// Resolve the artifact
		Artifact artifact = this.retrieveResolvedArtifact(groupId, artifactId,
				version, type, classifier);
		if (artifact == null) {
			return; // not able to resolve artifact so do not include
		}

		// Include the artifact
		this.includeClassPathEntry(artifact);

		// Resolve the pom for the artifact
		MavenProject pom = this.retrieveResolvedPom(groupId, artifactId,
				version);
		if (pom != null) {
			// Resolved so include dependencies
			this.includePomDependencies(getArtifactName(groupId, artifactId,
					version, type, classifier), pom);
		}
	}

	/**
	 * {@link OfficeFloorDecoratorContext}.
	 */
	private class DecoratorContext implements OfficeFloorDecoratorContext {

		/**
		 * Raw class path entry.
		 */
		private final String rawClassPathEntry;

		/**
		 * Resolved class path entries.
		 */
		public final List<String> resolvedClassPathEntries = new LinkedList<String>();

		/**
		 * Initiate.
		 * 
		 * @param rawClassPathEntry
		 *            Raw class path entry.
		 */
		public DecoratorContext(String rawClassPathEntry) {
			this.rawClassPathEntry = rawClassPathEntry;
		}

		/*
		 * ================== OfficeFloorDecoratorContext ==================
		 */

		@Override
		public String getRawClassPathEntry() {
			return this.rawClassPathEntry;
		}

		@Override
		public void includeResolvedClassPathEntry(String classpathEntry) {
			this.resolvedClassPathEntries.add(classpathEntry);
		}

		@Override
		public void setEnvironmentProperty(String name, String value) {
			// Load property if not overriding existing environment property
			if (!OfficeFloorCommandContextImpl.this.environment
					.containsKey(name)) {
				OfficeFloorCommandContextImpl.this.environment.setProperty(
						name, value);
			}
		}
	}

}