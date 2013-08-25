/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.security.scheme;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;

/**
 * Tests the {@link HttpAuthenticationScheme}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationSchemeTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * Ensure not authenticate if missing <code>Authenticate</code>
	 * {@link HttpHeader}.
	 */
	public void testMissingAuthenticateHttpHeader() throws Exception {
		this.record_getHeaders(null);
		this.assertAuthenticationScheme(null, null);
	}

	/**
	 * Ensure can obtain authenticate scheme without parameters.
	 */
	public void testAuthenticateSchemeOnly() throws Exception {
		this.record_getHeaders("Basic");
		this.assertAuthenticationScheme("Basic", null);
	}

	/**
	 * Ensure can obtain authenticate scheme with parameters.
	 */
	public void testAuthenticateSchemeWithParameters() throws Exception {
		this.record_getHeaders("Basic Base64UsernamePassword");
		this.assertAuthenticationScheme("Basic", "Base64UsernamePassword");
	}

	/**
	 * Ensure can authenticate with extra spacing.
	 */
	public void testExtraSpacing() throws Exception {
		this.record_getHeaders(" Basic  Base64UsernamePassword");
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
	private void assertAuthenticationScheme(String authenticationScheme,
			String parameters) {
		this.replayMockObjects();
		HttpAuthenticationScheme scheme = HttpAuthenticationScheme
				.getHttpAuthenticationScheme(this.request);
		if (scheme == null) {
			assertNull("Should not have authentication scheme", scheme);
		} else {
			assertEquals("Incorrect scheme", authenticationScheme,
					scheme.getAuthentiationScheme());
			assertEquals("Incorrect parameters", parameters,
					scheme.getParameters());
		}
		this.verifyMockObjects();
	}

	/**
	 * Records retrieving the {@link HttpHeader} instances from the
	 * {@link HttpRequest}.
	 * 
	 * @param authenticateValue
	 *            Value for the <code>Authenticate</code> {@link HttpHeader}.
	 *            <code>null</code> means to not add the {@link HttpHeader}.
	 * @return Listing of the {@link HttpHeader} instances being returned.
	 */
	private List<HttpHeader> record_getHeaders(String authenticateValue) {

		// Create the listing of the HTTP headers
		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);

		// Add the HTTP header (if have value)
		if (authenticateValue != null) {
			headers.add(new HttpHeaderImpl("Authorization", authenticateValue));
		}

		// Record obtaining the HTTP headers from request
		this.recordReturn(this.request, this.request.getHeaders(), headers);

		// Return the HTTP headers
		return headers;
	}

}