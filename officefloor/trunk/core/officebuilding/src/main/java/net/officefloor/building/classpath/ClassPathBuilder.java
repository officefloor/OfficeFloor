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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.officefloor.building.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectSorter;

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
		this.maven.setLocalRepositoryDirectory(localRepositoryDirectory);
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
		MavenProject pom = this.retrieveResolvedPom(groupId, artifactId,
				version);

		// Include the dependencies for the artifact (as per POM)
		this.includePomDependencies(pom);
	}

	/**
	 * Includes the Jar file and its dependencies on the class path.
	 * 
	 * @param jarFile
	 *            Jar file.
	 * @throws Exception
	 *             If fails to include the Jar file.
	 */
	public void includeJar(File jarFile) throws Exception {
		this.includeJar(jarFile, true);
	}

	/**
	 * Includes the Jar file in the class path.
	 * 
	 * @param jarFile
	 *            Jar file.
	 * @param isIncludeDependencies
	 *            Flag indicating to include the dependencies referenced by the
	 *            Jar file.
	 * @throws Exception
	 *             If fails to include the Jar file.
	 */
	public void includeJar(File jarFile, boolean isIncludeDependencies)
			throws Exception {

		// Include the Jar
		this.includeClassPathEntry(jarFile);

		// Determine if include dependencies
		if (!isIncludeDependencies) {
			return; // not include dependencies
		}

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
		MavenProject pomProject = this.maven
				.readProjectWithDependencies(pomFile);

		// Include the dependencies within POM
		this.includePomDependencies(pomProject);
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
	 * Closes and releases resources.
	 */
	public void close() throws Exception {
		this.maven.stop();
	}

	/**
	 * Includes the dependencies identified in the POM.
	 * 
	 * @param pom
	 *            {@link MavenProject} POM.
	 * @throws Exception
	 *             If fails to include the dependencies.
	 */
	private void includePomDependencies(MavenProject pom) throws Exception {

		// Obtain the artifacts
		@SuppressWarnings("unchecked")
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

			// Map the artifact by its POM
			pomToArtifact.put(dependencyPom, artifact);
		}

		// Sort the dependencies for correct class path order
		@SuppressWarnings("unchecked")
		List<MavenProject> sortedPoms = (List<MavenProject>) new ProjectSorter(
				new ArrayList<MavenProject>(pomToArtifact.keySet()))
				.getSortedProjects();

		// Include the artifacts in sorted order
		for (MavenProject project : sortedPoms) {
			Artifact artifact = pomToArtifact.get(project);
			this.includeClassPathEntry(artifact.getFile());
		}
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
	 * Retrieves the resolved POM {@link MavenProject}.
	 * 
	 * @param groupId
	 *            Group Id.
	 * @param artifactId
	 *            Artifact Id.
	 * @param version
	 *            Version.
	 * @return Resolved POM {@link MavenProject}.
	 * @throws Exception
	 *             If fails to retrieve the resolve POM {@link MavenProject}.
	 */
	private MavenProject retrieveResolvedPom(String groupId, String artifactId,
			String version) throws Exception {

		// Obtain the POM file
		Artifact pomArtifact = this.retrieveResolvedArtifact(groupId,
				artifactId, version, "pom", null);
		File pomFile = pomArtifact.getFile();

		// Read as Model (to determine if distribution status)
		Model model = this.maven.readModel(pomFile);
		DistributionManagement distribution = model.getDistributionManagement();
		if ((distribution != null) && (distribution.getStatus() != null)) {

			// Can not read Maven Project with distribution management status
			distribution.setStatus(null);

			// Create another POM file without the status
			File tempPomFile = File.createTempFile(OfficeBuilding.class
					.getSimpleName(), "pom");
			FileWriter writer = new FileWriter(tempPomFile);
			this.maven.writeModel(writer, model);
			writer.close();

			// Use the altered POM file
			pomFile = tempPomFile;
		}

		// Read in the Maven Project
		MavenProject pom = this.maven.readProjectWithDependencies(pomFile);
		return pom;
	}

}