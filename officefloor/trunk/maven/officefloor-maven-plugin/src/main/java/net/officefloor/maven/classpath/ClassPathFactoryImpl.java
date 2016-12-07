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
package net.officefloor.maven.classpath;

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
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

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
	public static String transformClassPathEntriesToClassPath(String... classPathEntries) {

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
	 * Creates a {@link PlexusContainer}.
	 * 
	 * @return {@link PlexusContainer}.
	 * @throws PlexusContainerException
	 *             If fails to create the {@link PlexusContainer}.
	 */
	public static PlexusContainer createPlexusContainer() throws PlexusContainerException {
		DefaultContainerConfiguration configuration = new DefaultContainerConfiguration();
		configuration.setClassPathScanning(PlexusConstants.SCANNING_INDEX);
		return new DefaultPlexusContainer(configuration);
	}

	/**
	 * Obtains the local repository as configured for the user.
	 * 
	 * @return Local repository.
	 * @throws Exception
	 *             If fails to obtain the user configured local repository.
	 */
	public static File getUserLocalRepository() throws Exception {

		// Obtain the local repository path
		String localRepositoryPath = null;

		// Obtain the settings
		File settingsFile = new File(org.apache.maven.repository.RepositorySystem.userMavenConfigurationHome,
				"settings.xml");
		if (settingsFile.exists()) {
			// Load user settings and obtain local repository
			SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
			SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
			request.setUserSettingsFile(settingsFile);
			SettingsBuildingResult result = settingsBuilder.build(request);
			localRepositoryPath = result.getEffectiveSettings().getLocalRepository();
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
	 *            {@link PlexusContainer}. May be <code>null</code>.
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
	public ClassPathFactoryImpl(PlexusContainer plexusContainer, RepositorySystem repositorySystem,
			File localRepository, RemoteRepository[] remoteRepositories) throws Exception {
		this.remoteRepositories = remoteRepositories;

		// Obtain the plexus container
		if (plexusContainer == null) {
			// Create the plexus container
			this.plexusContainer = createPlexusContainer();
		} else {
			// Use the specified plexus container
			this.plexusContainer = plexusContainer;
		}

		// Obtain the repository system
		this.repositorySystem = (repositorySystem != null ? repositorySystem
				: this.plexusContainer.lookup(RepositorySystem.class));

		// Obtain the local repository
		this.localRepository = (localRepository != null ? localRepository : getUserLocalRepository());
	}

	/**
	 * Initiate.
	 * 
	 * @param localRepository
	 *            Local repository. May be <code>null</code> to use default user
	 *            local repository.
	 * @param remoteRepositories
	 *            {@link RemoteRepository} instances.
	 * @throws Exception
	 *             If fails to initiate.
	 */
	public ClassPathFactoryImpl(File localRepository, RemoteRepository[] remoteRepositories) throws Exception {
		this(null, null, localRepository, remoteRepositories);
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

		// Create the repository session
		RepositorySystemSession session = this.createRepositorySystemSession();

		// Obtain the Maven Project
		ProjectBuilder builder = this.plexusContainer.lookup(ProjectBuilder.class);
		ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
		request.setRepositorySession(session);
		ProjectBuildingResult result = builder.build(pomFile, request);
		MavenProject project = result.getProject();

		// Return the Maven Project
		return project;
	}

	/**
	 * Creates the {@link RepositorySystemSession}.
	 * 
	 * @return {@link RepositorySystemSession}.
	 * @throws Exception
	 *             If fails to create {@link RepositorySystemSession}.
	 */
	private RepositorySystemSession createRepositorySystemSession() throws Exception {

		// Create the configured repository system session
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		LocalRepositoryManagerFactory factory = new EnhancedLocalRepositoryManagerFactory();
		session.setLocalRepositoryManager(factory.newInstance(session, new LocalRepository(this.localRepository)));

		// Return the repository system session
		return session;
	}

	/*
	 * ===================== ClassPathFactory ===========================
	 */

	@Override
	public String[] createArtifactClassPath(String artifactLocation) throws Exception {

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
				String[] artifactClassPath = this.createArtifactClassPath(groupId, artifactId, version, type, null);
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
	public String[] createArtifactClassPath(String groupId, String artifactId, String version, String type,
			String classifier) throws Exception {

		// Default type
		if (type == null) {
			type = "jar";
		}

		// Resolve the class path
		ArtifactClassPathResolver resolver = new ArtifactClassPathResolver(
				new DefaultArtifact(groupId, artifactId, classifier, type, version));
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
		for (Enumeration<? extends ZipEntry> iterator = archive.entries(); iterator.hasMoreElements();) {
			ZipEntry entry = iterator.nextElement();

			// Ensure pom.xml within the META-INF/maven location
			String name = entry.getName();
			if (name.startsWith("META-INF/maven/") && (name.endsWith("/pom.xml"))) {
				// Found the POM
				pomEntry = entry;
				break; // entry found
			}
		}
		if (pomEntry == null) {
			archive.close(); // ensure close archive
			return null; // no pom.xml
		}

		// Obtain model for POM
		ModelReader reader = this.plexusContainer.lookup(ModelReader.class);
		InputStream pomContents = archive.getInputStream(pomEntry);
		Model pomModel = reader.read(pomContents, null);
		pomContents.close();

		// Close the archive
		archive.close();

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
		 */
		public ArtifactClassPathResolver(Artifact artifact) {
			this.artifact = artifact;
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
					this.wait(100);
				}

				// Determine if timed out
				if (System.currentTimeMillis() > timeoutTime) {
					throw new Exception(
							"Timed out waiting for resolution of class path for artifact " + this.artifact.toString());
				}
			}
		}

		/*
		 * ====================== Runnable ============================
		 */

		@Override
		public void run() {
			try {

				// Create the repository session
				RepositorySystemSession session = ClassPathFactoryImpl.this.createRepositorySystemSession();

				// Create the dependency
				Dependency dependency = new Dependency(this.artifact, "run");

				// Create request to collect dependencies
				CollectRequest request = new CollectRequest();
				request.setRoot(dependency);
				Set<String> uniqueIds = new HashSet<String>();
				for (RemoteRepository remoteRepository : ClassPathFactoryImpl.this.remoteRepositories) {

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
					Builder remoteRepositoryBuilder = new Builder(uniqueId, remoteRepository.getType(),
							remoteRepository.getUrl());
					request.addRepository(remoteRepositoryBuilder.build());
				}

				// Collect the results
				CollectResult result = ClassPathFactoryImpl.this.repositorySystem.collectDependencies(session, request);
				DependencyNode node = result.getRoot();
				ClassPathFactoryImpl.this.repositorySystem.resolveDependencies(session,
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