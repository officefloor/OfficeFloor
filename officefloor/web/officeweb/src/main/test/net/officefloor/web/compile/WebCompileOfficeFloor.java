/*-
 * #%L
 * Web Plug-in
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
