/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.session;

import java.io.IOException;
import java.io.OutputStream;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponseCookie;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Tests the {@link HttpSessionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			this.server = MockHttpServer
					.configureMockHttpServer(context.getDeployedOffice().getDeployedOfficeInput("Servicer", "service"));
		});
		compile.office((context) -> {

			// Register the HTTP Session
			OfficeManagedObjectSource session = context.getOfficeArchitect()
					.addOfficeManagedObjectSource("HTTP_SESSION", HttpSessionManagedObjectSource.class.getName());
			session.setTimeout(1000);
			session.addOfficeManagedObject("HTTP_SESSION", ManagedObjectScope.PROCESS);

			// Register the servicer
			context.addSection("Servicer", Servicer.class);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure state is maintained by the {@link HttpSession} across multiple
	 * {@link HttpRequest} calls made by a user.
	 */
	public void testHttpSessionStateAcrossCalls() throws Exception {

		// Loop calling server
		HttpResponseCookie cookie = null;
		for (int i = 0; i < 10; i++) {

			// Create the request
			MockHttpRequestBuilder request = MockHttpServer.mockRequest();
			if (cookie != null) {
				request.cookie(cookie.getName(), cookie.getValue());
			}

			// Call the server
			MockHttpResponse response = this.server.send(request);
			int status = response.getStatus().getStatusCode();
			String callIndex = response.getEntity(null);

			// Obtain the cookie
			cookie = response.getCookie(HttpSessionManagedObjectSource.DEFAULT_SESSION_ID_COOKIE_NAME);
			assertNotNull("Should have session cookie on response", cookie);

			// Ensure results match and call index remembered by Session
			assertEquals("Call should be successful", 200, status);
			assertEquals("Incorrect call index", String.valueOf(i), callIndex);
		}
	}

	/**
	 * Services the {@link HttpRequest}.
	 */
	public static class Servicer {

		/**
		 * Services the {@link HttpRequest}.
		 * 
		 * @param connection {@link ServerHttpConnection}.
		 * @param session    {@link HttpSession}.
		 */
		public void service(ServerHttpConnection connection, HttpSession session) throws IOException {

			// Obtain the current call index
			Integer callIndex = (Integer) session.getAttribute("CALL_INDEX");

			// Increment the call index and store for next call
			callIndex = Integer.valueOf(callIndex == null ? 0 : (callIndex.intValue() + 1));
			session.setAttribute("CALL_INDEX", callIndex);

			// Return the call index
			OutputStream response = connection.getResponse().getEntity();
			response.write(String.valueOf(callIndex.intValue()).getBytes());
		}
	}

}
