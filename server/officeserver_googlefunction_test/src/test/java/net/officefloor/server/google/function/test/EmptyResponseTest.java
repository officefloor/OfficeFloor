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

import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Ensure provides 204 on empty response entity.
 */
public class EmptyResponseTest {

	public final @RegisterExtension @Order(0) GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			NoContentHttpFunction.class);

	private final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	private static final @RegisterExtension HttpClientExtension client = new HttpClientExtension();

	public static class NoContentHttpFunction implements HttpFunction {
		@Override
		public void service(HttpRequest request, HttpResponse response) throws Exception {
			// No content should have appropriate status code
		}
	}

	/**
	 * Ensure can continue to request the {@link HttpFunction}.
	 */
	@Test
	public void requestOnHttpFunction() throws Exception {
		org.apache.http.HttpResponse response = client.execute(new HttpGet("http://localhost:7878"));
		assertEquals(204, response.getStatusLine().getStatusCode(), "Should be no content success");
	}

}
