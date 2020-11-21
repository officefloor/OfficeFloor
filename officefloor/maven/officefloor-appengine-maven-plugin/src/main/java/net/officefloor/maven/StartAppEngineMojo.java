/*-
 * #%L
 * Maven OfficeFloor Plugin
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

package net.officefloor.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.cloud.tools.maven.run.RunAsyncMojo;
import com.google.datastore.v1.client.DatastoreHelper;

import net.officefloor.AppEngineSecureFilter;

/**
 * Starts the AppEngine with Datastore for integration testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartAppEngineMojo extends RunAsyncMojo {

	/**
	 * Obtains the AppEngine {@link Properties} {@link File}.
	 * 
	 * @param targetDir Target directory.
	 * @return AppEngine {@link Properties} {@link File}.
	 */
	public static File getAppEnginePropertiesFile(File targetDir) {
		return new File(targetDir, "officefloor-appengine.properties");
	}

	/**
	 * Port to run the Datastore on.
	 */
	@Parameter(property = "datastorePort")
	private Integer datastorePort = null;

	/**
	 * Target directory.
	 */
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File targetDir;

	/**
	 * Final name.
	 */
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String finalName;

	/*
	 * ========================== Mojo ===============================
	 */

	@Override
	public void execute() throws MojoExecutionException {

		// Determine the data store port
		if (this.datastorePort == null) {
			// Derive port from app engine port
			this.datastorePort = (this.getPort() != null) ? (this.getPort() + 1) : 8182;
		}

		// Start the local data store
		LocalDatastoreHelper localDataStore = LocalDatastoreHelper.create(this.datastorePort);
		try {
			localDataStore.start();
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed to start Datastore", ex);
		}
		this.getLog().info(
				"Datastore started on port " + this.datastorePort + " for project " + localDataStore.getProjectId());

		// Set up properties to connect to data store
		Properties properties = new Properties();
		BiConsumer<String, String> setProperty = (name, value) -> {
			properties.setProperty(name, value);
			this.getEnvironment().put(name, value);
		};
		setProperty.accept(DatastoreHelper.PROJECT_ID_ENV_VAR, localDataStore.getProjectId());
		setProperty.accept("DATASTORE_USE_PROJECT_ID_AS_APP_ID", "true");
		setProperty.accept(DatastoreHelper.LOCAL_HOST_ENV_VAR, "localhost:" + this.datastorePort);

		// Write the properties for stopping the local data store
		try {
			properties.store(new FileWriter(getAppEnginePropertiesFile(this.targetDir)), null);
		} catch (IOException ex) {
			throw new MojoExecutionException("Faile to store Datastore properties", ex);
		}

		// Obtain the services
		List<Path> paths = this.getServices();
		if ((paths == null) || (paths.size() == 0)) {
			paths = Arrays.asList(new File(this.targetDir, this.finalName).toPath());
		}

		// Load HTTPS web filter to services
		try {
			String filterClassPath = AppEngineSecureFilter.class.getName().replace('.', '/') + ".class";
			String requestClassPath = AppEngineSecureFilter.MockSecureHttpServletRequest.class.getName().replace('.',
					'/') + ".class";
			final String webInfClassesPrefix = "WEB-INF/classes/";
			NEXT_SERVICE: for (Path path : paths) {

				// Ensure directory exists for class
				Path classFilePath = path.resolve(webInfClassesPrefix + filterClassPath);
				if (Files.exists(classFilePath)) {
					continue NEXT_SERVICE; // already copied in
				}
				Files.createDirectories(classFilePath.getParent());

				// Copy in secure filter to avoid HTTPS redirects
				InputStream filterContent = AppEngineSecureFilter.class.getClassLoader()
						.getResourceAsStream(filterClassPath);
				if (filterContent == null) {
					throw new FileNotFoundException(
							"Can not find class resource " + AppEngineSecureFilter.class.getName());
				}
				Files.copy(filterContent, classFilePath);

				// Copy in required mock request class
				InputStream mockRequestContent = AppEngineSecureFilter.class.getClassLoader()
						.getResourceAsStream(requestClassPath);
				if (mockRequestContent == null) {
					throw new FileNotFoundException("Can not find class resource "
							+ AppEngineSecureFilter.MockSecureHttpServletRequest.class.getName());
				}
				Path requestFilePath = path.resolve(webInfClassesPrefix + requestClassPath);
				Files.copy(mockRequestContent, requestFilePath);
			}
		} catch (Exception ex) {
			throw new MojoExecutionException(
					"Failed to write in " + AppEngineSecureFilter.class.getSimpleName() + " to handle HTTPS", ex);
		}

		// Continue on to start the app engine
		super.execute();
	}

}