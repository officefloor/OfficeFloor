/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

/**
 * {@link ClassPathFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathFactoryImpl implements ClassPathFactory {

	/**
	 * Transforms the class path entries to a class path.
	 * 
	 * @param classPathEntries
	 *            Class path entries.
	 * @return Class path.
	 */
	public static String transformClassPathEntriesToClassPath(
			String... classPathEntries) {

		// Build the class path
		StringBuilder classPath = new StringBuilder();
		for (int i = 0; i < classPathEntries.length; i++) {

			// Include separator after first entry
			if (i > 0) {
				classPath.append(File.pathSeparator);
			}

			// Include the class path entry
			classPath.append(classPathEntries[i]);
		}

		// Return the class path
		return classPath.toString();
	}

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
	private final PlexusContainer plexusContainer;

	/**
	 * {@link RepositorySystem}.
	 */
	private final RepositorySystem repositorySystem;

	/**
	 * Local repository.
	 */
	private final File localRepository;

	/**
	 * {@link RemoteRepository} instances.
	 */
	private final RemoteRepository[] remoteRepositories;

	/**
	 * Initiate.
	 * 
	 * @param plexusContainer
	 *            {@link PlexusContainer}.
	 * @param repositorySystem
	 *            {@link RepositorySystem}. May be <code>null</code>.
	 * @param localRepository
	 *            Local repository. May be <code>null</code> to use default user
	 *            local repository.
	 * @param remoteRepositories
	 *            {@link RemoteRepository} instances.
	 * @throws Exception
	 *             If fails to initiate.
	 */
	public ClassPathFactoryImpl(PlexusContainer plexusContainer,
			RepositorySystem repositorySystem, File localRepository,
			RemoteRepository[] remoteRepositories) throws Exception {
		this.plexusContainer = plexusContainer;
		this.remoteRepositories = remoteRepositories;

		// Obtain the repository system
		this.repositorySystem = (repositorySystem != null ? repositorySystem
				: this.plexusContainer.lookup(RepositorySystem.class));

		// Obtain the local repository
		this.localRepository = (localRepository != null ? localRepository
				: getUserLocalRepository(this.plexusContainer));
	}

	/**
	 * Initiate.
	 * 
	 * @param plexusContainer
	 *            {@link PlexusContainer}.
	 * @param localRepository
	 *            Local repository. May be <code>null</code> to use default user
	 *            local repository.
	 * @param remoteRepositories
	 *            {@link RemoteRepository} instances.
	 * @throws Exception
	 *             If fails to initiate.
	 */
	public ClassPathFactoryImpl(PlexusContainer plexusContainer,
			File localRepository, RemoteRepository[] remoteRepositories)
			throws Exception {
		this(plexusContainer, null, localRepository, remoteRepositories);
	}

	/**
	 * Obtains the {@link MavenProject} for the <code>pom.xml</code>.
	 * 
	 * @param pomFile
	 *            <code>pom.xml</code> {@link File}.
	 * @return {@link MavenProject} for the <code>pom.xml</code>.
	 * @throws Exception
	 *             If fails to obtain the {@link MavenProject}.
	 */
	public MavenProject getMavenProject(File pomFile) throws Exception {

		// Create the Maven session
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		LocalRepository localRepository = new LocalRepository(
				this.localRepository);
		session.setLocalRepositoryManager(this.repositorySystem
				.newLocalRepositoryManager(localRepository));

		// Obtain the Maven Project
		ProjectBuilder builder = this.plexusContainer
				.lookup(ProjectBuilder.class);
		ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
		request.setRepositorySession(session);
		ProjectBuildingResult result = builder.build(pomFile, request);
		MavenProject project = result.getProject();

		// Return the Maven Project
		return project;
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

		// Resolve the class path
		ArtifactClassPathResolver resolver = new ArtifactClassPathResolver(
				new DefaultArtifact(groupId, artifactId, classifier, type,
						version), this.repositorySystem, this.localRepository,
				this.remoteRepositories);
		new Thread(resolver).start();

		// Return the resolved class path
		return resolver.blockWaitingForResolvedClassPath();
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
		ModelReader reader = this.plexusContainer.lookup(ModelReader.class);
		InputStream pomContents = archive.getInputStream(pomEntry);
		Model pomModel = reader.read(pomContents, null);
		pomContents.close();

		// Return the POM model
		return pomModel;
	}

	/**
	 * Resolver of the artifact class path.
	 */
	private class ArtifactClassPathResolver implements Runnable {

		/**
		 * {@link Artifact}.
		 */
		private final Artifact artifact;

		/**
		 * {@link RepositorySystem}.
		 */
		private final RepositorySystem repositorySystem;

		/**
		 * Local repository directory.
		 */
		private final File localRepositoryDirectory;

		/**
		 * Remote repositories.
		 */
		private final RemoteRepository[] remoteRepositories;

		/**
		 * Resolved class path entries.
		 */
		private String[] classPathEntries = null;

		/**
		 * Failure of resolving the class path.
		 */
		private Exception failure = null;

		/**
		 * Initiate.
		 * 
		 * @param artifact
		 *            {@link Artifact} to be resolved.
		 * @param repositorySystem
		 *            {@link RepositorySystem}.
		 * @param localRepositoryDirectory
		 *            Local repository directory.
		 * @param remoteRepositories
		 *            Listing of {@link RemoteRepository} instances.
		 */
		public ArtifactClassPathResolver(Artifact artifact,
				RepositorySystem repositorySystem,
				File localRepositoryDirectory,
				RemoteRepository[] remoteRepositories) {
			this.artifact = artifact;
			this.repositorySystem = repositorySystem;
			this.localRepositoryDirectory = localRepositoryDirectory;
			this.remoteRepositories = remoteRepositories;
		}

		/**
		 * Blocks waiting for resolution of the class path (or time out).
		 * 
		 * @return Resolved class path.
		 * @throws Exception
		 *             If fails to obtain the resolved class path.
		 */
		public String[] blockWaitingForResolvedClassPath() throws Exception {

			// Loop until complete or timed out (60 seconds)
			long timeoutTime = (System.currentTimeMillis() + (60L * 1000));
			for (;;) {

				// Determine if have resolved class path or failure
				synchronized (this) {

					// Determine if have resolved class path
					if (this.classPathEntries != null) {
						return this.classPathEntries;
					}

					// Determine if exception
					if (this.failure != null) {
						throw this.failure;
					}

					// Wait some time for resolution
					this.wait(1000);
				}

				// Determine if timed out
				if (System.currentTimeMillis() > timeoutTime) {
					throw new Exception(
							"Timed out waiting for resolution of class path for artifact "
									+ this.artifact.toString());
				}
			}
		}

		/*
		 * ====================== Runnable ============================
		 */

		@Override
		public void run() {
			try {

				// Create the maven session
				MavenRepositorySystemSession session = new MavenRepositorySystemSession();
				LocalRepository localRepository = new LocalRepository(
						this.localRepositoryDirectory);
				session.setLocalRepositoryManager(this.repositorySystem
						.newLocalRepositoryManager(localRepository));

				// Create the dependency
				Dependency dependency = new Dependency(this.artifact, "compile");

				// Create request to collect dependencies
				CollectRequest request = new CollectRequest();
				request.setRoot(dependency);
				Set<String> uniqueIds = new HashSet<String>();
				for (RemoteRepository remoteRepository : this.remoteRepositories) {

					// Obtain unique id for repository
					int index = 0;
					String prefix = remoteRepository.getId();
					String uniqueId = prefix;
					while (uniqueIds.contains(uniqueId)) {
						index++;
						uniqueId = prefix + index;
					}
					uniqueIds.add(uniqueId);

					// Add the remote repository
					request.addRepository(new org.sonatype.aether.repository.RemoteRepository(
							uniqueId, remoteRepository.getType(),
							remoteRepository.getUrl()));
				}

				// Collect the results
				CollectResult result = this.repositorySystem
						.collectDependencies(session, request);
				DependencyNode node = result.getRoot();
				this.repositorySystem.resolveDependencies(session,
						new DependencyRequest(node, null));

				// Generate the class path
				PreorderNodeListGenerator generator = new PreorderNodeListGenerator();
				node.accept(generator);
				String classPath = generator.getClassPath();

				// Split into class path entries
				synchronized (this) {
					this.classPathEntries = classPath.split(File.pathSeparator);
					this.notifyAll(); // notify class path available
				}

			} catch (Exception ex) {
				synchronized (this) {
					this.failure = ex;
					this.notifyAll(); // notify of failure
				}
			}
		}
	}

}