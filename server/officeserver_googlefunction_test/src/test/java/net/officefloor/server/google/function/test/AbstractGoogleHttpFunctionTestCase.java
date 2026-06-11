/*-
 * #%L
 * Google Function Testing
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.http.HttpClientTestUtil;

/**
 * Abstract functionality for testing {@link HttpFunction}.
 */
public class AbstractGoogleHttpFunctionTestCase {

	/**
	 * Undertakes the test.
	 * 
	 * @param isSecure If HTTPS.
	 * @param port     Port that {@link HttpFunction} is running on.
	 */
	protected void doTest(boolean isSecure, int port) throws Exception {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(isSecure)) {
			HttpResponse response = client.execute(new HttpGet((isSecure ? "https" : "http") + "://localhost:" + port));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
			assertEquals("TEST", entity, "Incorrect response");
		}
	}

}
