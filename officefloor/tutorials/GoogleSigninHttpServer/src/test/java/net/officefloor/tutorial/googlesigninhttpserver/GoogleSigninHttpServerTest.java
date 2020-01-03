/*-
 * #%L
 * Google Signin Tutorial
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

package net.officefloor.tutorial.googlesigninhttpserver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.identity.google.mock.GoogleIdTokenRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginRequest;
import net.officefloor.tutorial.googlesigninhttpserver.LoginLogic.LoginResponse;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Google Sign-in HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleSigninHttpServerTest {

	// START SNIPPET: tutorial
	private GoogleIdTokenRule googleSignin = new GoogleIdTokenRule();

	private MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain order = RuleChain.outerRule(this.googleSignin).around(this.server);

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void ensureLogin() throws Exception {

		// Create mock token
		String token = this.googleSignin.getMockIdToken("TEST", "mock@officefloor.net");

		// Ensure can login
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/login").method(HttpMethod.POST)
				.header("Content-Type", "application/json").entity(mapper.writeValueAsString(new LoginRequest(token))));
		response.assertResponse(200, mapper.writeValueAsString(new LoginResponse("mock@officefloor.net")));
	}
	// END SNIPPET: tutorial

}
