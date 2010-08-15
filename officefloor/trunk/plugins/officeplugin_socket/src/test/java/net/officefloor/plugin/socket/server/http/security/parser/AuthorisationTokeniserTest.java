/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.security.parser;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AuthorisationTokeniser}.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthorisationTokeniserTest extends OfficeFrameTestCase {

	/**
	 * {@link AuthorisationTokenHandler}.
	 */
	private final AuthorisationTokenHandler handler = this
			.createMock(AuthorisationTokenHandler.class);

	/**
	 * Ensure can handle <code>null</code> value.
	 */
	public void testNullValue() {
		this.doTest(null);
	}

	/**
	 * Ensure can handle <code>Basic</code> authentication.
	 */
	public void testBasic() throws Exception {
		this.handler.handleAuthenticationScheme("Basic");
		this.handler.handleParameter(AuthorisationTokenHandler.BASIC_USER_ID,
				"Aladdin");
		this.handler.handleParameter(AuthorisationTokenHandler.BASIC_PASSWORD,
				"open sesame");
		this.doTest("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
	}

	/**
	 * Ensure can handle <code>Basic</code> authentication with extra spacing.
	 */
	public void testBasicExtraSpacing() throws Exception {
		this.handler.handleAuthenticationScheme("Basic");
		this.handler.handleParameter(AuthorisationTokenHandler.BASIC_USER_ID,
				"Aladdin");
		this.handler.handleParameter(AuthorisationTokenHandler.BASIC_PASSWORD,
				"open sesame");
		this.doTest("Basic    QWxhZGRpbjpvcGVuIHNlc2FtZQ==   ");
	}

	/**
	 * Ensure can handle no parameters.
	 */
	public void testParameters_None() throws Exception {
		this.handler.handleAuthenticationScheme("S");
		this.doTest("S");
	}

	/**
	 * Ensure can handle no parameters with trailing space.
	 */
	public void testParameters_NoneWithSpacing() throws Exception {
		this.handler.handleAuthenticationScheme("S");
		this.doTest("S ");
	}

	/**
	 * Ensure can handle parameters.
	 */
	public void testParameters_Combination() throws Exception {
		this.handler.handleAuthenticationScheme("S");
		this.handler.handleParameter("a", "b"); // simple value
		this.handler.handleParameter("c", "d"); // simple quoting
		this.handler.handleParameter("e", " f "); // spaced quoting
		this.handler.handleParameter("g", "h"); // space value
		this.handler.handleParameter("i j", "k"); // name containing space
		this.doTest("S a=b , c=\"d\", e = \" f \" , g = h , i j=k");
	}

	/**
	 * Ensure can handle last character being quote.
	 */
	public void testParameters_LastQuote() throws Exception {
		this.handler.handleAuthenticationScheme("S");
		this.handler.handleParameter("a", "b");
		this.doTest("S a=\"b\"");
	}

	/**
	 * Ensure can handle last character space after quote.
	 */
	public void testParameters_SpaceAfterQuote() throws Exception {
		this.handler.handleAuthenticationScheme("S");
		this.handler.handleParameter("a", "b");
		this.doTest("S a=\"b\" ");
	}

	/**
	 * Ensure can handle last character being last character of value.
	 */
	public void testParameters_Value() throws Exception {
		this.handler.handleAuthenticationScheme("S");
		this.handler.handleParameter("a", "b");
		this.doTest("S a=b");
	}

	/**
	 * Ensure can handle last character being space after value.
	 */
	public void testParameters_SpaceAfterValue() throws Exception {
		this.handler.handleAuthenticationScheme("S");
		this.handler.handleParameter("a", "b");
		this.doTest("S a=b ");
	}

	/**
	 * Ensure can handle <code>Digest</code> authentication example.
	 */
	public void testDigestExample() throws Exception {
		this.handler.handleAuthenticationScheme("Digest");
		this.handler.handleParameter("username", "Mufasa");
		this.handler.handleParameter("realm", "testrealm@host.com");
		this.handler.handleParameter("nonce",
				"dcd98b7102dd2f0e8b11d0f600bfb0c093");
		this.handler.handleParameter("uri", "/dir/index.html");
		this.handler.handleParameter("qop", "auth");
		this.handler.handleParameter("nc", "00000001");
		this.handler.handleParameter("cnonce", "0a4f113b");
		this.handler.handleParameter("response",
				"6629fae49393a05397450978507c4ef1");
		this.handler.handleParameter("opaque",
				"5ccc069c403ebaf9f0171e9517f40e41");
		this
				.doTest("Digest username=\"Mufasa\", realm=\"testrealm@host.com\","
						+ " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", uri=\"/dir/index.html\","
						+ " qop=auth, nc=00000001, cnonce=\"0a4f113b\","
						+ " response=\"6629fae49393a05397450978507c4ef1\","
						+ " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
	}

	/**
	 * Does the test.
	 * 
	 * @param headerValue
	 *            Header value.
	 */
	private void doTest(String headerValue) {
		try {
			this.replayMockObjects();

			// Create the tokeniser
			AuthorisationTokeniser tokeniser = new AuthorisationTokeniserImpl();

			// Tokenise the header value
			tokeniser.tokeniseAuthorizationHeaderValue(headerValue,
					this.handler);

			this.verifyMockObjects();
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}