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

package net.officefloor.web.compile;

import java.util.function.Consumer;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;

/**
 * Provides {@link WebArchitect} and server configuration for testing web
 * applications.
 * 
 * @author Daniel Sagenschneider
 */
public class WebCompileOfficeFloor extends CompileOfficeFloor {

	/**
	 * Context path. May be <code>null</code>.
	 */
	private final String contextPath;

	/**
	 * Instantiate with no context path.
	 */
	public WebCompileOfficeFloor() {
		this(null);
	}

	/**
	 * Instantiate with context path.
	 * 
	 * @param contextPath Context path.
	 */
	public WebCompileOfficeFloor(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Adds a {@link CompileWebExtension}.
	 * 
	 * @param extension {@link CompileWebExtension}.
	 */
	public void web(CompileWebExtension extension) {
		// Wrap web extension into office extension
		this.office((context) -> {
			WebArchitect webArchitect = WebArchitectEmployer.employWebArchitect(WebCompileOfficeFloor.this.contextPath,
					context.getOfficeArchitect(), context.getOfficeSourceContext());
			CompileWebContextImpl web = new CompileWebContextImpl(context, webArchitect);
			if (extension != null) {
				// Allow no configuration except default web
				extension.extend(web);
			}
			webArchitect.informOfficeArchitect();
		});
	}

	/**
	 * Loads {@link MockHttpServer}.
	 * 
	 * @param consumeMockhttpServer Receives the {@link MockHttpServer}.
	 */
	public void mockHttpServer(Consumer<MockHttpServer> consumeMockhttpServer) {
		this.officeFloor((context) -> {
			MockHttpServer server = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
			if (consumeMockhttpServer != null) {
				consumeMockhttpServer.accept(server);
			}
		});
	}

}
