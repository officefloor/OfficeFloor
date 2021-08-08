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
		File propertiesFile = AppEngineUtil.getAppEnginePropertiesFile(new File(".", "target"));
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
