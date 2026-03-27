/*-
 * #%L
 * OfficeFloor integration of WAR
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

package net.officefloor.webapp;

import static org.junit.Assert.assertNotEquals;

import org.apache.catalina.connector.Connector;

import jakarta.servlet.Servlet;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.servlet.archive.TutorialArchiveLocatorUtil;
import net.officefloor.tutorial.warapp.ServletDependency;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can provide {@link OfficeFloor} {@link Connector}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletTest extends OfficeFrameTestCase {

	/**
	 * Ensure can service simple GET.
	 */
	public void testSimpleGet() throws Exception {
		this.doWarTest("/simple", "SIMPLE");
	}

	/**
	 * Ensure can inject into WAR {@link Servlet} instances.
	 */
	public void testInjectGet() throws Exception {
		this.doWarTest("/inject", new OverrideServletDependency().getMessage());
	}

	/**
	 * Undertakes WAR {@link Servlet} test.
	 * 
	 * @param path           Path to {@link Servlet}.
	 * @param expectedEntity Expected entity in response.
	 */
	private void doWarTest(String path, String expectedEntity) throws Exception {

		// Ensure valid test
		ServletDependency injectedDependency = new OverrideServletDependency();
		assertNotEquals("INVALID TEST: should have different inject message", new ServletDependency().getMessage(),
				injectedDependency.getMessage());

		// Undertake test
		String webAppPath = TutorialArchiveLocatorUtil.getArchiveFile("WarApp", ".war").getAbsolutePath();
		CompileWoof compile = new CompileWoof(true);
		compile.office((context) -> {
			Singleton.load(context.getOfficeArchitect(), injectedDependency);
		});
		try (MockWoofServer server = compile.open(OfficeFloorWar.PROPERTY_WAR_PATH, webAppPath)) {
			MockHttpResponse response = server.send(MockHttpServer.mockRequest(path));
			response.assertResponse(200, expectedEntity);
		}
	}

	/**
	 * Provide own {@link ServletDependency}.
	 */
	private static class OverrideServletDependency extends ServletDependency {

		@Override
		public String getMessage() {
			return "OFFICEFLOOR INJECT";
		}
	}

}
