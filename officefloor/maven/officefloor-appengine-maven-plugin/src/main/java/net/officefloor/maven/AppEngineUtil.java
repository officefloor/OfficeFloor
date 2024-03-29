/*-
 * #%L
 * OfficeFloor AppEngine Maven Plugin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.maven;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;

import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.cloud.tools.maven.run.AbstractRunMojo;
import com.google.datastore.v1.client.DatastoreHelper;

/**
 * Utility functionality for starting/running App Engine.
 * 
 * @author Daniel Sagenschneider
 */
public class AppEngineUtil {

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
	 * Sets up the App Engine for running/starting.
	 * 
	 * @param targetDir     Target directory.
	 * @param finalName     Final name.
	 * @param datastorePort Port to run datastore on. May be <code>null</code>.
	 * @param plugin        {@link PluginDescriptor}.
	 * @param mojo          {@link AbstractRunMojo}.
	 * @throws MojoExecutionException If fails to setup App Engine.
	 */
	public static void setupAppEngine(File targetDir, String finalName, Integer datastorePort, PluginDescriptor plugin,
			AbstractRunMojo mojo) throws MojoExecutionException {

		// Determine the data store port
		if (datastorePort == null) {
			// Derive port from app engine port
			datastorePort = (mojo.getPort() != null) ? (mojo.getPort() + 1) : 8182;
		}

		// Start the local data store
		LocalDatastoreHelper localDataStore = LocalDatastoreHelper.create(datastorePort);
		try {
			localDataStore.start();
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed to start Datastore", ex);
		}
		mojo.getLog()
				.info("Datastore started on port " + datastorePort + " for project " + localDataStore.getProjectId());

		// Set up properties to connect to data store
		Properties properties = new Properties();
		BiConsumer<String, String> setProperty = (name, value) -> {
			properties.setProperty(name, value);
			mojo.getEnvironment().put(name, value);
		};
		setProperty.accept(DatastoreHelper.PROJECT_ID_ENV_VAR, localDataStore.getProjectId());
		setProperty.accept("DATASTORE_USE_PROJECT_ID_AS_APP_ID", "true");
		setProperty.accept(DatastoreHelper.LOCAL_HOST_ENV_VAR, "localhost:" + datastorePort);

		// Write the properties for stopping the local data store
		try {
			properties.store(new FileWriter(getAppEnginePropertiesFile(targetDir)), null);
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to store Datastore properties", ex);
		}

		// Obtain the services
		List<Path> servicePaths = mojo.getServices();
		if ((servicePaths == null) || (servicePaths.size() == 0)) {
			servicePaths = Arrays.asList(new File(targetDir, finalName).toPath());
		}

		// Obtain the appengine emulator enhancement
		File officeServerAppEngineEmulatorJar = getOfficeFloorAppEngineEmulatorJar(plugin);

		// Determine if already available
		mojo.getLog().info("Incorporating " + officeServerAppEngineEmulatorJar.getName());
		File webLibDir = new File(new File(targetDir, finalName), "WEB-INF/lib");
		File targetJarFile = new File(webLibDir, officeServerAppEngineEmulatorJar.getName());
		if (!targetJarFile.exists()) {

			// Copy the jar to lib directory
			try {
				Files.createDirectories(webLibDir.toPath());
				Files.copy(officeServerAppEngineEmulatorJar.toPath(), targetJarFile.toPath());
			} catch (IOException ex) {
				throw new MojoExecutionException("Failed to copy in AppEngine enhancements", ex);
			}
		}
	}

	/**
	 * Tears down the App Engine.
	 * 
	 * @param targetDir Target directory.
	 * @param finalName Final name.
	 * @param plugin    {@link PluginDescriptor}.
	 * @throws MojoExecutionException If fails to tear down App Engine.
	 */
	public static void tearDownAppEngine(File targetDir, String finalName, PluginDescriptor plugin, Log log)
			throws MojoExecutionException {

		// Obtain the location of datastore
		String location;
		try {
			File appEnginePropertiesFile = getAppEnginePropertiesFile(targetDir);
			Properties properties = new Properties();
			properties.load(new FileReader(appEnginePropertiesFile));
			location = properties.getProperty(DatastoreHelper.LOCAL_HOST_ENV_VAR);
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to obtain datastore location", ex);
		}

		// Stop the data store
		log.info("Stopping datastore " + location);
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			client.execute(new HttpPost("http://" + location + "/shutdown"));
		} catch (NoHttpResponseException ex) {
			// Ignore as may shutdown before sending response
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to shutdown datastore", ex);
		}

		// Remove the appengine emulator enhancement
		File officeServerAppEngineEmulatorJar = getOfficeFloorAppEngineEmulatorJar(plugin);
		File webLibDir = new File(new File(targetDir, finalName), "WEB-INF/lib");
		File targetJarFile = new File(webLibDir, officeServerAppEngineEmulatorJar.getName());
		if (targetJarFile.exists()) {

			// Attempt to delete file (on windows may be locked, so try for a bit)
			log.info("Removing " + officeServerAppEngineEmulatorJar.getName());
			long startTime = System.currentTimeMillis();
			for (;;) {

				// Wait some time (allowing shutdown)
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					// Continue one
				}

				// Attempt to delete
				boolean isDeleted = false;
				IOException failure = null;
				try {
					Files.delete(targetJarFile.toPath());
					isDeleted = true;
				} catch (IOException ex) {
					failure = ex;
				}

				// If delete, nothing further
				if (isDeleted) {
					return;
				}

				// Determine if timed out attempting to delete
				if ((startTime + 10_000) < System.currentTimeMillis()) {
					throw new MojoExecutionException("Failed to clean AppEngine enhancements", failure);
				}
			}
		}
	}

	/**
	 * Obtains the {@link File} to the OfficeFloor AppEngine Emulator Jar.
	 * 
	 * @param plugin {@link PluginDescriptor}.
	 * @return {@link File} to the OfficeFloor AppEngine Emulator Jar.
	 * @throws MojoExecutionException
	 */
	private static File getOfficeFloorAppEngineEmulatorJar(PluginDescriptor plugin) throws MojoExecutionException {

		// Obtain the appengine emulator enhancement
		File officeServerAppEngineEmulatorJar = null;
		for (Artifact artifact : plugin.getArtifacts()) {
			if (("net.officefloor.server".equals(artifact.getGroupId()))
					&& ("officeserver_appengineemulator".equals(artifact.getArtifactId()))) {
				officeServerAppEngineEmulatorJar = artifact.getFile();
			}
		}
		if (officeServerAppEngineEmulatorJar == null) {
			throw new MojoExecutionException("Failed to obtain AppEngine enhancement jar");
		}
		return officeServerAppEngineEmulatorJar;
	}

}
