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
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.cloud.tools.maven.run.StopMojo;
import com.google.datastore.v1.client.DatastoreHelper;

/**
 * Stops the AppEngine with Datastore for integration testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopAppEngineMojo extends StopMojo {

	/**
	 * Target directory.
	 */
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File targetDir;

	/*
	 * ========================== Mojo =================================
	 */

	@Override
	public void execute() throws MojoExecutionException {

		// Stop the AppEngine
		super.execute();

		// Obtain the location of datastore
		String location;
		try {
			File appEnginePropertiesFile = AppEngineUtil.getAppEnginePropertiesFile(this.targetDir);
			Properties properties = new Properties();
			properties.load(new FileReader(appEnginePropertiesFile));
			location = properties.getProperty(DatastoreHelper.LOCAL_HOST_ENV_VAR);
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to obtain datastore location", ex);
		}

		// Stop the data store
		this.getLog().info("Stopping datastore " + location);
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			client.execute(new HttpPost("http://" + location + "/shutdown"));
		} catch (NoHttpResponseException ex) {
			// Ignore as may shutdown before sending response
		} catch (IOException ex) {
			throw new MojoExecutionException("Failed to shutdown datastore", ex);
		}
	}

}