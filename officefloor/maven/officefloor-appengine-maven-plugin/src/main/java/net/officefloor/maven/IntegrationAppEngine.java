/*-
 * #%L
 * OfficeFloor AppEngine Maven Plugin
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

import com.google.cloud.NoCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.datastore.v1.client.DatastoreHelper;

/**
 * Means for integration tests to obtain access to AppEngine and Datastore.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrationAppEngine {

	/**
	 * Obtains the {@link Datastore}.
	 * 
	 * @return {@link Datastore}.
	 * @throws Exception If fails to obtain the {@link Datastore}.
	 */
	public static Datastore getDatastore() throws Exception {

		// Obtain the properties
		Properties properties = getStartProperties();

		// Obtain data store details
		String projectId = properties.getProperty(DatastoreHelper.PROJECT_ID_ENV_VAR);
		String host = properties.getProperty(DatastoreHelper.LOCAL_HOST_ENV_VAR);

		// Create and return the data store
		return DatastoreOptions.newBuilder().setProjectId(projectId).setHost(host)
				.setCredentials(NoCredentials.getInstance()).setRetrySettings(ServiceOptions.getNoRetrySettings())
				.build().getService();
	}

	/**
	 * Obtains the start properties.
	 * 
	 * @return Start properties.
	 * @throws IOException If fails to load the properties.
	 */
	private static Properties getStartProperties() throws IOException {

		// Ensure have start properties
		File propertiesFile = StartAppEngineMojo.getAppEnginePropertiesFile(new File(".", "target"));
		if (!propertiesFile.exists()) {
			throw new IllegalStateException("Please ensure AppEngine started, as can not find properties file at "
					+ propertiesFile.getAbsolutePath());
		}

		// Load the properties
		Properties properties = new Properties();
		properties.load(new FileReader(propertiesFile));

		// Return the properties
		return properties;
	}

	/**
	 * All access via static methods.
	 */
	private IntegrationAppEngine() {
	}

}
