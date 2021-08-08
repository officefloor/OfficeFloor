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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.test.system.EnvironmentRule;
import net.officefloor.test.system.SystemPropertiesRule;

/**
 * Abstract test case.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTestCase extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
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

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open(commandLineProperties);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/" + path));
			assertEquals("Incorrect entity", expectedEntity, HttpClientTestUtil.entityToString(response));
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
