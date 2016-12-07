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
package net.officefloor.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.command.parameters.KeyStoreOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.KeyStorePasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.OfficeBuildingPortOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.PasswordOfficeFloorCommandParameter;
import net.officefloor.building.command.parameters.UsernameOfficeFloorCommandParameter;
import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.process.ProcessConfiguration;
import net.officefloor.building.process.ProcessOutputStreamFactory;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.maven.classpath.ClassPathFactory;
import net.officefloor.maven.classpath.ClassPathFactoryImpl;
import net.officefloor.maven.classpath.RemoteRepository;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystem;

/**
 * Maven goal to start the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "start", requiresDependencyResolution = ResolutionScope.COMPILE)
public class StartOfficeBuildingGoal extends AbstractGoal {

	/**
	 * Default {@link OfficeBuilding} port.
	 */
	public static final Integer DEFAULT_OFFICE_BUILDING_PORT = Integer
			.valueOf(OfficeBuildingPortOfficeFloorCommandParameter.DEFAULT_OFFICE_BUILDING_PORT);

	/**
	 * Makes the default key store file available.
	 * 
	 * @return Key store {@link File}.
	 * @throws MojoFailureException
	 *             If fails to obtain the key store {@link File}.
	 */
	public static File getKeyStoreFile() throws MojoFailureException {
		try {
			return KeyStoreOfficeFloorCommandParameter.getDefaultKeyStoreFile();
		} catch (IOException ex) {
			throw new MojoFailureException(
					"Failed making default key store available: "
							+ ex.getMessage() + " [" + ex.getClass().getName()
							+ "]");
		}
	}

	/**
	 * Key store {@link File} password.
	 */
	public static final String KEY_STORE_PASSWORD = KeyStorePasswordOfficeFloorCommandParameter.DEFAULT_KEY_STORE_PASSWORD;

	/**
	 * Client user name.
	 */
	public static final String USER_NAME = UsernameOfficeFloorCommandParameter.DEFAULT_USER_NAME;

	/**
	 * Client password.
	 */
	public static final String PASSWORD = PasswordOfficeFloorCommandParameter.DEFAULT_PASSWORD;

	/**
	 * Creates the {@link StartOfficeBuildingGoal} with the required parameters.
	 * 
	 * @param project
	 *            {@link MavenProject}.
	 * @param pluginDependencies
	 *            Plug-in dependencies.
	 * @param localRepository
	 *            Local repository.
	 * @param repositorySystem
	 *            {@link RepositorySystem}.
	 * @param plexusContainer
	 *            {@link PlexusContainer}.
	 * @param log
	 *            {@link Log}.
	 * @return {@link StartOfficeBuildingGoal}.
	 */
	public static StartOfficeBuildingGoal createStartOfficeBuildingGoal(
			MavenProject project, List<Artifact> pluginDependencies,
			ArtifactRepository localRepository,
			RepositorySystem repositorySystem, PlexusContainer plexusContainer,
			Log log) {
		StartOfficeBuildingGoal goal = new StartOfficeBuildingGoal();
		goal.project = project;
		goal.pluginDependencies = pluginDependencies;
		goal.localRepository = localRepository;
		goal.repositorySystem = repositorySystem;
		goal.plexusContainer = plexusContainer;
		goal.setLog(log);
		return goal;
	}

	/**
	 * {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	/**
	 * Plug-in dependencies.
	 */
	@Parameter(defaultValue = "${plugin.artifacts}", required = true)
	private List<Artifact> pluginDependencies;

	/**
	 * Local repository.
	 */
	@Parameter(defaultValue = "${localRepository}", required = true)
	private ArtifactRepository localRepository;

	/**
	 * {@link RepositorySystem}.
	 */
	@Component
	private RepositorySystem repositorySystem;

	/**
	 * {@link PlexusContainer}.
	 */
	@Component
	private PlexusContainer plexusContainer;

	/**
	 * Port to run the {@link OfficeBuilding} on.
	 */
	@Parameter(property = "port")
	private Integer port = DEFAULT_OFFICE_BUILDING_PORT;

	/**
	 * {@link PluginDependencyInclusion} instances.
	 */
	private final List<PluginDependencyInclusion> dependencyInclusions = new ArrayList<PluginDependencyInclusion>(
			2);

	/**
	 * Initiate.
	 */
	public StartOfficeBuildingGoal() {
		// Always include the OfficeBuilding
		this.includePluginDependencyToOfficeBuildingClassPath(
				OfficeBuildingManager.OFFICE_BUILDING_GROUP_ID,
				OfficeBuildingManager.OFFICE_BUILDING_ARTIFACT_ID, "jar", null);
	}

	/**
	 * <p>
	 * Provides ability to selectively include plugin dependencies on the class
	 * path of the started {@link OfficeBuilding}.
	 * <p>
	 * This allows for additional plugins such {@link OfficeFloorDecorator}
	 * instances.
	 * <p>
	 * Please note that it must be a dependency of the plugin. Not found
	 * dependencies will result in a build failure.
	 * <p>
	 * The version is derived from the maven configuration.
	 * 
	 * @param groupId
	 *            Group Id of the dependency.
	 * @param artifactId
	 *            Artifact Id of the dependency.
	 * @param type
	 *            Type of the dependency.
	 * @param classifier
	 *            Classifier. May be <code>null</code>.
	 */
	public void includePluginDependencyToOfficeBuildingClassPath(
			String groupId, String artifactId, String type, String classifier) {
		this.dependencyInclusions.add(new PluginDependencyInclusion(groupId,
				artifactId, type, classifier));
	}

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have configured values
		ensureNotNull("Must have project", this.project);
		ensureNotNull("Must have plug-in dependencies", this.pluginDependencies);
		ensureNotNull("Must have local repository", this.localRepository);
		ensureNotNull("Must have repository system", this.repositorySystem);
		ensureNotNull(
				"Port not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.port);

		// Indicate the configuration
		final Log log = this.getLog();
		log.debug(OfficeBuilding.class.getSimpleName() + " configuration:");
		log.debug("\tPort = " + this.port);

		// Create the environment properties
		Properties environment = new Properties();
		environment.putAll(this.project.getProperties());

		// Log the properties
		log.debug("\tProperties:");
		for (String propertyName : environment.stringPropertyNames()) {
			log.debug("\t\t" + propertyName + " = "
					+ environment.getProperty(propertyName));
		}

		// Obtain the plugin dependency inclusions
		List<Artifact> artifactInclusions = new ArrayList<Artifact>(
				this.pluginDependencies.size());
		for (PluginDependencyInclusion inclusion : this.dependencyInclusions) {

			// Must match on dependency for inclusion
			Artifact includedDependency = null;
			for (Artifact dependency : this.pluginDependencies) {
				if ((inclusion.groupId.equals(dependency.getGroupId()))
						&& (inclusion.artifactId.equals(dependency
								.getArtifactId()))
						&& (inclusion.type.equals(dependency.getType()))
						&& ((inclusion.classifier == null) || (inclusion.classifier
								.equals(dependency.getClassifier())))) {
					// Found the dependency to include
					includedDependency = dependency;
				}
			}

			// Ensure have dependency for inclusion
			if (includedDependency == null) {
				throw newMojoExecutionException(
						"Failed to obtain plug-in dependency "
								+ inclusion.groupId
								+ ":"
								+ inclusion.artifactId
								+ (inclusion.classifier == null ? "" : ":"
										+ inclusion.classifier) + ":"
								+ inclusion.type, null);

			}

			// Include the dependency
			artifactInclusions.add(includedDependency);
		}

		// Obtain the class path for OfficeBuilding
		String classPath = null;
		String[] remoteRepositoryURLs;
		try {

			// Indicate the remote repositories
			log.debug("\tRemote repositories:");

			// Obtain remote repositories and load to class path factory
			List<RemoteRepository> remoteRepositories = new LinkedList<RemoteRepository>();
			List<String> urls = new LinkedList<String>();
			for (Object object : this.project.getRemoteArtifactRepositories()) {
				ArtifactRepository repository = (ArtifactRepository) object;
				String remoteRepositoryUrl = repository.getUrl();
				remoteRepositories.add(new RemoteRepository(repository.getId(),
						repository.getLayout().getId(), remoteRepositoryUrl));
				urls.add(remoteRepositoryUrl);

				// Indicate the remote repository
				log.debug("\t\t" + remoteRepositoryUrl);
			}
			remoteRepositoryURLs = urls.toArray(new String[urls.size()]);

			// Create the class path factory and add remote repositories
			File localRepositoryDirectory = new File(
					this.localRepository.getBasedir());
			ClassPathFactory classPathFactory = new ClassPathFactoryImpl(
					this.plexusContainer, this.repositorySystem,
					localRepositoryDirectory,
					remoteRepositories
							.toArray(new RemoteRepository[remoteRepositories
									.size()]));

			// Indicate the class path
			log.debug("\tClass path:");

			// Obtain the class path entries for each included artifact
			List<String> classPathEntries = new LinkedList<String>();
			for (Artifact dependency : artifactInclusions) {

				// Obtain the class path entries for the dependency
				String[] entries = classPathFactory.createArtifactClassPath(
						dependency.getGroupId(), dependency.getArtifactId(),
						dependency.getVersion(), dependency.getType(),
						dependency.getClassifier());

				// Uniquely include the class path entries
				for (String entry : entries) {
					if (classPathEntries.contains(entry)) {
						continue; // ignore as already included
					}
					classPathEntries.add(entry);

					// Indicate class path entry
					log.debug("\t\t" + entry);
				}
			}

			// Obtain the class path
			classPath = ClassPathFactoryImpl
					.transformClassPathEntriesToClassPath(classPathEntries
							.toArray(new String[classPathEntries.size()]));

		} catch (Exception ex) {
			throw newMojoExecutionException(
					"Failed obtaining dependencies for launching OfficeBuilding",
					ex);
		}

		// Create the process configuration
		ProcessConfiguration configuration = new ProcessConfiguration();
		configuration.setAdditionalClassPath(classPath);

		// Write output to file
		configuration
				.setProcessOutputStreamFactory(new ProcessOutputStreamFactory() {

					@Override
					public OutputStream createStandardProcessOutputStream(
							String processNamespace, String[] command)
							throws IOException {

						// Log the command
						StringBuilder commandLine = new StringBuilder();
						commandLine.append(OfficeBuilding.class.getSimpleName()
								+ " command line:");
						for (String commandItem : command) {
							commandLine.append(" ");
							commandLine.append(commandItem);
						}
						log.debug(commandLine);

						// Return the output stream
						return this.getOutputStream(processNamespace);
					}

					@Override
					public OutputStream createErrorProcessOutputStream(
							String processNamespace) throws IOException {
						return this.getOutputStream(processNamespace);
					}

					/**
					 * Lazy instantiated {@link OutputStream}.
					 */
					private OutputStream outputStream = null;

					/**
					 * Obtains the {@link OutputStream}.
					 * 
					 * @param processNamespace
					 *            Process name space.
					 * @return {@link OutputStream}.
					 * @throws IOException
					 *             If fails to obtain the {@link OutputStream}.
					 */
					private synchronized OutputStream getOutputStream(
							String processNamespace) throws IOException {

						// Lazy instantiate the output stream
						if (this.outputStream == null) {

							// Create the output file
							File file = File.createTempFile(processNamespace,
									".log");
							this.outputStream = new FileOutputStream(file);

							// Log that outputting to file
							log.info("Logging "
									+ OfficeBuilding.class.getSimpleName()
									+ " output to file "
									+ file.getAbsolutePath());
						}

						// Return the output stream
						return this.outputStream;
					}
				});

		// Start the OfficeBuilding
		try {
			OfficeBuildingManager.spawnOfficeBuilding(null,
					this.port.intValue(), getKeyStoreFile(),
					KEY_STORE_PASSWORD, USER_NAME, PASSWORD, null, false,
					environment, null, true, remoteRepositoryURLs,
					configuration);
		} catch (Throwable ex) {
			// Provide details of the failure
			final String MESSAGE = "Failed starting the "
					+ OfficeBuilding.class.getSimpleName();
			this.getLog().error(
					MESSAGE + ": " + ex.getMessage() + " ["
							+ ex.getClass().getSimpleName() + "]");
			this.getLog().error("DIAGNOSIS INFORMATION:");
			this.getLog().error(
					"   classpath='" + System.getProperty("java.class.path")
							+ "'");
			this.getLog().error("   additional classpath='" + classPath + "'");

			// Propagate the failure
			throw newMojoExecutionException(MESSAGE, ex);
		}

		// Log started OfficeBuilding
		this.getLog().info(
				"Started " + OfficeBuilding.class.getSimpleName() + " on port "
						+ this.port.intValue());
	}

	/**
	 * Plugin dependency inclusion.
	 */
	private static class PluginDependencyInclusion {

		/**
		 * Group Id.
		 */
		public final String groupId;

		/**
		 * Artifact Id.
		 */
		public final String artifactId;

		/**
		 * Type.
		 */
		public final String type;

		/**
		 * Classifier. May be <code>null</code>.
		 */
		public final String classifier;

		/**
		 * Initiate.
		 * 
		 * @param groupId
		 *            Group Id.
		 * @param artifactId
		 *            Artifact Id.
		 * @param type
		 *            Type.
		 * @param classifier
		 *            Classifier. May be <code>null</code>.
		 */
		public PluginDependencyInclusion(String groupId, String artifactId,
				String type, String classifier) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.type = type;
			this.classifier = classifier;
		}
	}

}