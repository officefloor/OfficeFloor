/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import java.io.IOException;
import java.util.Properties;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.test.system.EnvironmentRule;
import net.officefloor.test.system.SystemPropertiesRule;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract test case.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractModelTestCase {

	public static final String NO_COMPOSITION_PROPERTY_NAME = ApplicationOfficeFloorSource.OFFICE_NAME + "." + WoofLoaderOfficeExtensionService.OFFICE_FLOOR_DIRECTORY_PROPERTY;
	public static final String NO_COMPOSITION_PROPERTY_VALUE = "not/load/composition";

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@BeforeEach
	public void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Undertakes request.
	 * 
	 * @param path           Path.
	 * @param expectedEntity Expected entity.
	 */
	protected void doRequestTest(String path, String expectedEntity, String... commandLineProperties)
			throws IOException {

		// Avoid composition loading
		String[] properties = new String[commandLineProperties.length + 4];
		properties[0] = NO_COMPOSITION_PROPERTY_NAME;
		properties[1] = NO_COMPOSITION_PROPERTY_VALUE;
		properties[2] = SecondOfficeSetup.SECOND_OFFICE_NAME + "." + WoofLoaderOfficeExtensionService.OFFICE_FLOOR_DIRECTORY_PROPERTY;
		properties[3] = NO_COMPOSITION_PROPERTY_VALUE;
        System.arraycopy(commandLineProperties, 0, properties, 4, commandLineProperties.length);

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open(properties);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/" + path));
			assertEquals(expectedEntity, HttpClientTestUtil.entityToString(response), "Incorrect entity");
		}
	}

	/**
	 * Undertakes properties test with {@link System} {@link Properties}.
	 * 
	 * @param path                         Path.
	 * @param expectedEntity               Expected entity.
	 * @param systemPropertyNameValuePairs {@link System} {@link Properties}
	 *                                     name/value pairs.
	 */
	protected void doSystemPropertiesTest(String path, String expectedEntity, String... systemPropertyNameValuePairs)
			throws IOException {
		new SystemPropertiesRule(systemPropertyNameValuePairs).run(() -> {
			this.doRequestTest(path, expectedEntity);
		});
	}

	/**
	 * Undertakes environment test.
	 * 
	 * @param path                      Path.
	 * @param expectedEntity            Expected entity.
	 * @param environmentNameValuePairs Environment name/value pairs.
	 */
	protected void doEnvironmentTest(String path, String expectedEntity, String... environmentNameValuePairs)
			throws Exception {
		new EnvironmentRule(environmentNameValuePairs).run(() -> {
			this.doRequestTest(path, expectedEntity);
		});
	}

}
