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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.officefloor.building.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

/**
 * Builds the class path for running the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathBuilder {

	/**
	 * Default listing of artifact types that are not to be included on the
	 * realised class path.
	 */
	public String[] DEFAULT_IGNORED_TYPES = new String[] { "pom" };

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
	 * Listing of artifact types that are not to be included on the realised
	 * class path.
	 */
	private final Set<String> ignoredTypes = new HashSet<String>(Arrays
			.asList(DEFAULT_IGNORED_TYPES));

	/**
	 * Listing of class path entries in order for the realised class path.
	 */
	private final List<String> classPathEntries = new LinkedList<String>();

	/**
	 * Initiate.
	 * 
	 * @param localRepositoryDirectory
	 *            Local {@link ArtifactRepository}.
	 * @param remoteRepositoryUrls
	 *            Remote {@link ArtifactRepository} instances.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public ClassPathBuilder(File localRepositoryDirectory,
			String... remoteRepositoryUrls) throws Exception {

		// Create the Maven Embedder
		this.maven = new MavenEmbedder();
		this.maven.setClassLoader(this.getClass().getClassLoader());
		this.maven.setGlobalChecksumPolicy(null);
		this.maven.start();
		try {

			// Create the local repository
			this.localRepository = this.maven
					.createLocalRepository(localRepositoryDirectory);

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
	 * Convenience include {@link Artifact} method that defaults:
	 * <ol>
	 * <li><code>type</code> = <code>&quot;jar&quot;</code></li>
	 * <li><code>classifier</code> = <code>null</code></li>
	 * </ol>
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version.
	 * @throws Exception
	 *             If fails to include the {@link Artifact} as its dependencies.
	 */
	public void includeArtifact(String groupId, String artifactId,
			String version) throws Exception {
		this.includeArtifact(groupId, artifactId, version, "jar", null);
	}

	/**
	 * Includes the particular {@link Artifact} and its dependencies on the
	 * class path.
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
	 * @throws Exception
	 *             If fails to include the {@link Artifact} and its dependencies
	 *             on the class path.
	 */
	public void includeArtifact(String groupId, String artifactId,
			String version, String type, String classifier) throws Exception {

		// Do not include if ignored type
		if (!this.ignoredTypes.contains(type)) {
			// Retrieve the artifact for inclusion on class path
			Artifact artifact = this.retrieveResolvedArtifact(groupId,
					artifactId, version, type, classifier);

			// Artifact available, so include on class path
			this.includeClassPathEntry(artifact.getFile());
		}

		// Obtain the POM model for the artifact
		Artifact pomArtifact = this.retrieveResolvedArtifact(groupId,
				artifactId, version, "pom", classifier);
		Model pom = this.maven.readModel(pomArtifact.getFile());

		// Include the dependencies for the artifact (as per POM)
		this.includePomDependencies(pom);
	}

	/**
	 * Includes the Jar file in the class path.
	 * 
	 * @param jarFile
	 *            Jar file.
	 * @throws Exception
	 *             If fails to include the Jar file.
	 */
	public void includeJar(File jarFile) throws Exception {

		// Include the Jar
		this.includeClassPathEntry(jarFile);

		// Obtain the pom.xml file from within the Jar
		JarEntry pom = null;
		JarFile jar = new JarFile(jarFile);
		for (Enumeration<JarEntry> iterator = jar.entries(); iterator
				.hasMoreElements();) {
			JarEntry entry = iterator.nextElement();

			// Ensure pom.xml within the META-INF/maven location
			String name = entry.getName();
			if (name.startsWith("META-INF/maven/")
					&& (name.endsWith("/pom.xml"))) {
				// Found the pom
				pom = entry;
				break; // entry found
			}
		}
		if (pom == null) {
			return; // no POM, so no dependencies
		}

		// Obtain the POM to obtain dependencies
		InputStream pomContents = jar.getInputStream(pom);

		// Make the POM contents available as file for reading
		File pomFile = File.createTempFile(
				OfficeBuilding.class.getSimpleName(), "pom");
		OutputStream pomOutput = new FileOutputStream(pomFile);
		for (int value = pomContents.read(); value != -1; value = pomContents
				.read()) {
			pomOutput.write(value);
		}
		pomOutput.close();
		pomContents.close();

		// Read in the POM
		Model pomModel = this.maven.readModel(pomFile);

		// Include the dependencies within POM
		this.includePomDependencies(pomModel);
	}

	/**
	 * Includes the directory in the class path.
	 * 
	 * @param directory
	 *            Directory.
	 * @throws Exception
	 *             If fails to include the directory.
	 */
	public void includeDirectory(File directory) throws Exception {

		// Ensure the exists and a directory
		if (!directory.isDirectory()) {
			throw new FileNotFoundException("Directory not exists: '"
					+ directory.getPath() + "'");
		}

		// Include the directory
		this.includeClassPathEntry(directory);
	}

	/**
	 * Includes the {@link ClassPathSeed} entries.
	 * 
	 * @param seed
	 *            {@link ClassPathSeed}.
	 * @throws Exception
	 *             If fails to include the seeding.
	 */
	public void includeSeed(ClassPathSeed seed) throws Exception {
		seed.include(this);
	}

	/**
	 * Includes the entry in the built class path.
	 * 
	 * @param classPathEntry
	 *            Class path entry.
	 * @throws Exception
	 *             If fails to include class path entry.
	 */
	public void includeClassPathEntry(File classPathEntry) throws Exception {

		// Ensure the class path entry exists
		if (!classPathEntry.exists()) {
			throw new FileNotFoundException("Class path entry not exists: '"
					+ classPathEntry.getPath() + "'");
		}

		// Obtain the canonical path for comparison
		String entry = classPathEntry.getCanonicalPath();

		// Ensure not already added
		if (this.classPathEntries.contains(entry)) {
			return; // entry already in class path
		}

		// Include the class path entry
		this.classPathEntries.add(entry);
	}

	/**
	 * Obtains the built class path.
	 * 
	 * @return Built class path.
	 */
	public String getBuiltClassPath() {

		// Obtain the path separator
		String separator = System.getProperty("path.separator");

		// Build the class path
		StringBuilder path = new StringBuilder();
		boolean isFirst = true;
		for (String entry : this.classPathEntries) {

			// Provide separator between entries
			if (!isFirst) {
				path.append(separator);
			}
			isFirst = false; // for next iteration

			// Add the class path entry
			path.append(entry);
		}

		// Return the class path
		return path.toString();
	}

	/**
	 * Closes and releases resources.
	 */
	public void close() throws Exception {
		this.maven.stop();
	}

	/**
	 * Includes the dependencies identified in the POM {@link Model}.
	 * 
	 * @param pom
	 *            POM {@link Model}.
	 * @throws Exception
	 *             If fails to include the dependencies.
	 */
	private void includePomDependencies(Model pom) throws Exception {

		// Include the dependencies identified in the POM
		List<?> dependencies = pom.getDependencies();
		for (Object item : dependencies) {
			Dependency dependency = (Dependency) item;

			// Retrieve the dependency artifact for inclusion on class path
			Artifact dependencyArtifact = this.retrieveResolvedDependency(
					dependency, pom);

			// Include the dependency artifact (and its dependencies)
			this.includeArtifact(dependencyArtifact.getGroupId(),
					dependencyArtifact.getArtifactId(), dependencyArtifact
							.getVersion(), dependencyArtifact.getType(),
					dependencyArtifact.getClassifier());
		}
	}

	/**
	 * Retrieves the resolved {@link Artifact} for the {@link Dependency}.
	 * 
	 * @param dependency
	 *            {@link Dependency}.
	 * @param pom
	 *            POM {@link Model} containing the {@link Dependency}.
	 * @return Resolved {@link Artifact} for the dependency.
	 * @throws Exception
	 *             If fails to resolve the version.
	 */
	private Artifact retrieveResolvedDependency(Dependency dependency, Model pom)
			throws Exception {

		// Obtain details of dependency
		String groupId = this.translate(dependency.getGroupId(), pom);
		String artifactId = this.translate(dependency.getArtifactId(), pom);
		String version = this.translate(dependency.getVersion(), pom);
		String type = this.translate(dependency.getType(), pom);
		String classifier = this.translate(dependency.getClassifier(), pom);

		// Determine if have version
		if ((version == null) || (version.trim().length() == 0)) {
			// Resolve version from dependency management
			version = this.resolveVersion(groupId, artifactId, pom);
		}

		// Retrieve the resolved dependency artifact
		Artifact artifact = this.retrieveResolvedArtifact(groupId, artifactId,
				version, type, classifier);

		// Return the resolved dependency artifact
		return artifact;
	}

	/**
	 * Resolves the version for the {@link Artifact} via
	 * {@link DependencyManagement}.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param pom
	 *            {@link Model} to be used for {@link DependencyManagement}.
	 * @return Resolved version.
	 * @throws Exception
	 *             If fails to resolve the version.
	 */
	private String resolveVersion(String groupId, String artifactId, Model pom)
			throws Exception {

		// Ensure have POM otherwise no further parent to resolve version
		if (pom == null) {
			throw new ArtifactResolutionException(
					"Can not determine version for artifact '" + groupId + ":"
							+ artifactId + "'", groupId, artifactId, null,
					null, null);
		}

		// Determine if dependency management in POM
		DependencyManagement management = pom.getDependencyManagement();
		if (management != null) {
			for (Object object : management.getDependencies()) {
				Dependency dependency = (Dependency) object;

				// Determine if matching artifact
				if ((dependency.getGroupId().equals(groupId))
						&& (dependency.getArtifactId().equals(artifactId))) {
					// Obtain version via dependency management
					String version = dependency.getVersion();

					// Return the translated version
					return this.translate(version, pom);
				}
			}
		}

		// No dependency management, try parent
		Parent parent = pom.getParent();
		Model parentPom = null;
		if (parent != null) {
			// Have parent so obtain its model
			Artifact parentArtifact = this.retrieveResolvedArtifact(parent
					.getGroupId(), parent.getArtifactId(), parent.getVersion(),
					"pom", null);
			parentPom = this.maven.readModel(parentArtifact.getFile());
		}

		// Attempt parent POM for version
		return this.resolveVersion(groupId, artifactId, parentPom);
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
	 * @throws Exception
	 *             If fails to retrieve the resolved {@link Artifact}.
	 */
	private Artifact retrieveResolvedArtifact(String groupId,
			String artifactId, String version, String type, String classifier)
			throws Exception {
		Artifact artifact = this.maven.createArtifactWithClassifier(groupId,
				artifactId, version, type, classifier);
		this.maven.resolve(artifact, this.remoteRepositories,
				this.localRepository);
		return artifact;
	}

	/**
	 * Translates the value.
	 * 
	 * @param value
	 *            Value.
	 * @param properties
	 *            Properties.
	 * @return Translated value.
	 */
	private String translate(String value, Model pom) throws Exception {
		if (value == null) {
			return value;
		}

		// Obtain the properties
		Properties properties = pom.getProperties();

		// Add groupId
		String groupId = pom.getGroupId();
		if (groupId == null) {
			groupId = pom.getParent().getGroupId();
		}
		properties.setProperty("project.groupId", groupId);
		properties.setProperty("pom.groupId", groupId);

		// Add version
		String version = pom.getVersion();
		if (version == null) {
			version = pom.getParent().getVersion();
		}
		properties.setProperty("project.version", version);
		properties.setProperty("pom.version", version);

		// Transform the value
		for (String name : properties.stringPropertyNames()) {
			String property = properties.getProperty(name);
			value = value.replace("${" + name + "}", property);
		}

		// Return the value
		return value;
	}

}