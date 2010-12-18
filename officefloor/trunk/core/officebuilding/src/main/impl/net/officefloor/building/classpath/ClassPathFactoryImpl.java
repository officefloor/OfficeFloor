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
package net.officefloor.building.classpath;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

/**
 * {@link ClassPathFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathFactoryImpl implements ClassPathFactory {

	/**
	 * Obtains the local repository as configured for the user.
	 * 
	 * @param plexusContainer
	 *            {@link PlexusContainer}.
	 * @return Local repository.
	 * @throws Exception
	 *             If fails to obtain the user configured local repository.
	 */
	public static File getUserLocalRepository(PlexusContainer plexusContainer)
			throws Exception {

		// Obtain the local repository path
		String localRepositoryPath = null;

		// Obtain the settings
		File settingsFile = new File(
				org.apache.maven.repository.RepositorySystem.userMavenConfigurationHome,
				"settings.xml");
		if (settingsFile.exists()) {
			// Load user settings and obtain local repository
			SettingsBuilder settingsBuilder = plexusContainer
					.lookup(SettingsBuilder.class);
			SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
			request.setUserSettingsFile(settingsFile);
			SettingsBuildingResult result = settingsBuilder.build(request);
			localRepositoryPath = result.getEffectiveSettings()
					.getLocalRepository();
		}

		// Use default local repository (if not specified in settings)
		if (localRepositoryPath == null) {
			localRepositoryPath = org.apache.maven.repository.RepositorySystem.defaultUserLocalRepository
					.getAbsolutePath();
		}

		// Return the user local repository
		return new File(localRepositoryPath);
	}

	/**
	 * {@link PlexusContainer}.
	 */
	private PlexusContainer plexusContainer;

	/**
	 * Local repository.
	 */
	private final File localRepository;

	/**
	 * Remote repositories.
	 */
	private final List<RemoteRepository> remoteRepositories = new LinkedList<RemoteRepository>();

	/**
	 * Initiate.
	 * 
	 * @param localRepository
	 *            Local repository. May be <code>null</code> to use default user
	 *            local repository.
	 * @throws Exception
	 *             If fails to initiate.
	 */
	public ClassPathFactoryImpl(PlexusContainer plexusContainer,
			File localRepository) throws Exception {
		this.plexusContainer = plexusContainer;
		this.localRepository = (localRepository != null ? localRepository
				: getUserLocalRepository(this.plexusContainer));
	}

	/**
	 * Registers a {@link RemoteRepository}.
	 * 
	 * @param id
	 *            Id of repository.
	 * @param type
	 *            Type of repository. May be <code>null</code>.
	 * @param url
	 *            URL of repository.
	 */
	public void registerRemoteRepository(String id, String type, String url) {
		this.remoteRepositories.add(new RemoteRepository(id,
				(type != null ? type : "default"), url));
	}

	/*
	 * ===================== ClassPathFactory ===========================
	 */

	@Override
	public String[] createArtifactClassPath(String artifactLocation)
			throws Exception {

		// Create the listing of class paths
		List<String> classPath = new LinkedList<String>();

		// Ensure artifact first on class path
		classPath.add(artifactLocation);

		// Obtain the artifact details (if available)
		File artifactFile = new File(artifactLocation);
		if (artifactFile.exists()) {

			// Obtain model from artifact
			Model pom = this.getArchivePom(artifactFile);
			if (pom != null) {

				// Obtain the details of the artifact
				Parent parent = pom.getParent();
				String artifactId = pom.getArtifactId();
				String groupId = pom.getGroupId();
				if ((groupId == null) && (parent != null)) {
					groupId = parent.getGroupId();
				}
				String version = pom.getVersion();
				if ((version == null) && (parent != null)) {
					version = parent.getVersion();
				}
				String type = pom.getPackaging();

				// Obtain the class path for the artifact
				String[] artifactClassPath = this.createArtifactClassPath(
						groupId, artifactId, version, type, null);
				for (String classPathEntry : artifactClassPath) {
					if (!classPath.contains(classPathEntry)) {
						classPath.add(classPathEntry);
					}
				}
			}
		}

		// Return the class path
		return classPath.toArray(new String[classPath.size()]);
	}

	@Override
	public String[] createArtifactClassPath(String groupId, String artifactId,
			String version, String type, String classifier) throws Exception {

		// Default type
		if (type == null) {
			type = "jar";
		}

		// Obtain the repository system
		RepositorySystem system = plexusContainer
				.lookup(RepositorySystem.class);

		// Create the maven session
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		LocalRepository localRepository = new LocalRepository(
				this.localRepository);
		session.setLocalRepositoryManager(system
				.newLocalRepositoryManager(localRepository));

		// Create the dependency
		Dependency dependency = new Dependency(new DefaultArtifact(groupId,
				artifactId, classifier, type, version), "compile");

		// Create request to collect dependencies
		CollectRequest request = new CollectRequest();
		request.setRoot(dependency);
		for (RemoteRepository remoteRepository : this.remoteRepositories) {
			request.addRepository(remoteRepository);
		}

		// Collect the results
		CollectResult result = system.collectDependencies(session, request);
		DependencyNode node = result.getRoot();
		system.resolveDependencies(session, node, null);

		// Generate the class path
		PreorderNodeListGenerator generator = new PreorderNodeListGenerator();
		node.accept(generator);
		String classPath = generator.getClassPath();

		// Split into class path entries
		String[] classPathEntries = classPath.split(File.pathSeparator);

		// Return the class path entries
		return classPathEntries;
	}

	/**
	 * Obtains the {@link Model} (POM) from the archive.
	 * 
	 * @param archiveFile
	 *            Archive {@link File}.
	 * @return {@link Model}.
	 * @throws Exception
	 *             If fails to obtain the POM {@link Model}.
	 */
	private Model getArchivePom(File archiveFile) throws Exception {

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
				// Found the POM
				pomEntry = entry;
				break; // entry found
			}
		}
		if (pomEntry == null) {
			return null; // no pom.xml
		}

		// Obtain model for POM
		ModelReader reader = plexusContainer.lookup(ModelReader.class);
		InputStream pomContents = archive.getInputStream(pomEntry);
		Model pomModel = reader.read(pomContents, null);
		pomContents.close();

		// Return the POM model
		return pomModel;
	}

}