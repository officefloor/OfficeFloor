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

package net.officefloor.web.security.integrate;

import java.io.IOException;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Abstract functionality for integration testing of the
 * {@link HttpSecuritySource} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSecurityIntegrateTestCase extends OfficeFrameTestCase {

	/**
	 * {@link MockHttpServer}.
	 */
	protected MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Configure the application
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.mockHttpServer((server) -> this.server = server);
		compiler.web((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();

			// Employ security architect
			HttpSecurityArchitect securityArchitect = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web,
					context.getOfficeArchitect(), context.getOfficeSourceContext());

			// Configure the HTTP Security
			this.configureHttpSecurity(context, securityArchitect);

			// Add servicing methods
			OfficeSection section = context.addSection("SERVICE", Servicer.class);
			office.link(web.getHttpInput(false, "/service").getInput(), section.getOfficeSectionInput("service"));
			office.link(web.getHttpInput(false, "/logout").getInput(), section.getOfficeSectionInput("logout"));

			// Inform web architect of security
			securityArchitect.informWebArchitect();
		});

		// Start the office
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
	}

	/**
	 * Configures the {@link HttpSecurity}.
	 * 
	 * @param context           {@link CompileWebContext}.
	 * @param securityArchitect {@link HttpSecurityArchitect}
	 * @return Configured {@link HttpSecurity}.
	 */
	protected abstract HttpSecurityBuilder configureHttpSecurity(CompileWebContext context,
			HttpSecurityArchitect securityArchitect);

	@Override
	protected void tearDown() throws Exception {
		// Stop server
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Undertakes the request.
	 * 
	 * @param path   Path.
	 * @param status Expected status.
	 * @param entity Expected entity.
	 * @return {@link MockHttpResponse}.
	 */
	protected MockHttpResponse doRequest(String path, int status, String entity) {
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest(path));
		response.assertResponse(status, entity);
		return response;
	}

	/**
	 * Undertakes the request with cookies from previous request.
	 * 
	 * @param path     Path.
	 * @param response Previous response.
	 * @param status   Expected response status.
	 * @param entity   Expected response entity.
	 */
	protected MockHttpResponse doRequest(String path, MockHttpResponse response, int status, String entity) {

		// Undertake the request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(path).cookies(response);
		response = this.server.send(request);

		// Ensure appropriate response
		response.assertResponse(status, entity);

		// Return the response
		return response;
	}

	/**
	 * Services the {@link HttpRequest}.
	 */
	public static class Servicer {

		/**
		 * Services the {@link HttpRequest}.
		 * 
		 * @param acessControl {@link HttpAccessControl} dependency ensures
		 *                     authentication before servicing.
		 * @param connection   {@link ServerHttpConnection}.
		 * @throws IOException If fails.
		 */
		public void service(HttpAccessControl acessControl, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("Serviced for " + acessControl.getPrincipal().getName());
		}

		/**
		 * Undertakes logging out.
		 * 
		 * @param authentication {@link HttpAuthentication}.
		 * @param connection     {@link ServerHttpConnection}.
		 * @throws IOException If fails.
		 */
		public void logout(HttpAuthentication<HttpCredentials> authentication, ServerHttpConnection connection)
				throws IOException {

			// Log out
			authentication.logout(null);

			// Indicate logged out
			connection.getResponse().getEntityWriter().write("LOGOUT");
		}
	}

}
