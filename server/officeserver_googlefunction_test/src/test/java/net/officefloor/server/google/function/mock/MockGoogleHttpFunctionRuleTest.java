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

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the {@link MockGoogleHttpFunctionRule}.
 */
public class MockGoogleHttpFunctionRuleTest {

	public final @Rule(order = 0) MockGoogleHttpFunctionRule httpFunction = new MockGoogleHttpFunctionRule(
			TestHttpFunction.class);

	public final @Rule(order = 1) OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Test
	public void request() {
		MockHttpResponse response = httpFunction.send(MockHttpServer.mockRequest());
		response.assertResponse(200, "TEST");
	}

	@Test
	public void requestSecure() {
		String url = httpFunction.getMockHttpServer().createClientUrl(true, "/");
		MockHttpResponse response = httpFunction.send(MockHttpServer.mockRequest(url).secure(true));
		response.assertResponse(200, "TEST-secure");
	}

}
