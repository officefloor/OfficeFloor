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

package net.officefloor.server.google.function.mock;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.google.function.AbstractExtendWithHttpServerTestCase;
import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Ensure able to extend with {@link HttpServer}.
 */
public class MockExtendWithHttpServerTest extends AbstractExtendWithHttpServerTestCase {

	private static final @RegisterExtension @Order(0) MockGoogleHttpFunctionExtension httpFunction = extendWithHttpServer(
			new MockGoogleHttpFunctionExtension(TestHttpFunction.class));

	/**
	 * Ensure can continue to request the {@link HttpFunction}.
	 */
	@Test
	public void requestOnHttpFunction() throws Exception {
		MockHttpResponse response = httpFunction.send(MockHttpServer.mockRequest());
		response.assertResponse(200, "TEST");
	}

}
