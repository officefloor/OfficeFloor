/*-
 * #%L
 * JWT Separate Authority Server Tutorial (Resource Server)
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

package net.officefloor.tutorial.jwtresourcehttpserver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.jwt.mock.MockJwtAccessTokenRule;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the JWT Resource HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class JwtResourceHttpServerTest {

	// Sets up server to accept created JWT access tokens for testing
	public MockJwtAccessTokenRule authority = new MockJwtAccessTokenRule();

	public MockWoofServerRule server = new MockWoofServerRule();

	@Rule
	public RuleChain orderedRules = RuleChain.outerRule(this.authority).around(this.server);

	@Test
	public void ensureResourceSecured() throws Exception {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/resource").secure(true));
		response.assertResponse(401, "");
	}

	@Test
	public void accessSecureResource() throws Exception {

		// Create mock access token
		String accessToken = this.authority.createAccessToken(new Claims("daniel", new String[] { "tutorial" }));

		// Access the secured resource
		MockHttpResponse response = this.server.send(
				MockHttpServer.mockRequest("/resource").secure(true).header("authorization", "Bearer " + accessToken));
		response.assertResponse(200, "Hello daniel");
	}

}
// END SNIPPET: tutorial
