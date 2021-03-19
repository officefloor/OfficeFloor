/*-
 * #%L
 * Web Security
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
