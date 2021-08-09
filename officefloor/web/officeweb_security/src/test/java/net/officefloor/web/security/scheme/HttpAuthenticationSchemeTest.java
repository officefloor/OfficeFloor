/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.scheme;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Tests the {@link HttpAuthenticationScheme}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationSchemeTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpRequest}.
	 */
	private final MockHttpRequestBuilder request = MockHttpServer.mockRequest();

	/**
	 * Ensure not authenticate if missing <code>Authenticate</code>
	 * {@link HttpHeader}.
	 */
	public void testMissingAuthenticateHttpHeader() throws Exception {
		this.assertAuthenticationScheme(null, null);
	}

	/**
	 * Ensure can obtain authenticate scheme without parameters.
	 */
	public void testAuthenticateSchemeOnly() throws Exception {
		this.request.header("Authorization", "Basic");
		this.assertAuthenticationScheme("Basic", null);
	}

	/**
	 * Ensure can obtain authenticate scheme with parameters.
	 */
	public void testAuthenticateSchemeWithParameters() throws Exception {
		this.request.header("Authorization", "Basic Base64UsernamePassword");
		this.assertAuthenticationScheme("Basic", "Base64UsernamePassword");
	}

	/**
	 * Ensure can authenticate with extra spacing.
	 */
	public void testExtraSpacing() throws Exception {
		this.request.header("Authorization", " Basic  Base64UsernamePassword");
		this.assertAuthenticationScheme("Basic", " Base64UsernamePassword");
	}

	/**
	 * Asserts the authentication scheme.
	 * 
	 * @param authenticationScheme
	 *            Expectd authentication scheme.
	 * @param parameters
	 *            Expected parameters.
	 */
	private void assertAuthenticationScheme(String authenticationScheme, String parameters) {
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme.getHttpAuthenticationScheme(this.request.build());
		if (scheme == null) {
			assertNull("Should not have authentication scheme", scheme);
		} else {
			assertEquals("Incorrect scheme", authenticationScheme, scheme.getAuthentiationScheme());
			assertEquals("Incorrect parameters", parameters, scheme.getParameters());
		}
	}

}
