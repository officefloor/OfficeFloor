/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.compile;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorExtension;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.test.system.SystemPropertiesRule;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.woof.MockPropertySectionSource;
import net.officefloor.woof.WoOF;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link CompileWoof}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileWoofTest extends OfficeFrameTestCase {

	/**
	 * Ensure can link web.
	 */
	public void testWebExtension() throws Exception {
		CompileWoof compiler = new CompileWoof();
		compiler.web((context) -> {
			context.link(false, "POST", "/test", ServiceLink.class);
		});
		try (MockWoofServer server = compiler.open()) {
			MockWoofResponse response = server
					.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/test", new Payload("TEST")));
			response.assertJson(200, new Payload("RESPONSE TEST"));
		}
	}

	public static class ServiceLink {
		public void service(Payload request, ObjectResponse<Payload> response) {
			response.send(new Payload("RESPONSE " + request.getMessage()));
		}
	}

	/**
	 * Ensure can extend WoOF.
	 */
	public void testWoofExtension() throws Exception {
		CompileWoof compiler = new CompileWoof();
		Closure<Boolean> isRun = new Closure<>(false);
		compiler.woof((context) -> {
			assertNotNull("Should have Office architect", context.getOfficeArchitect());
			assertNotNull("Should have Office context", context.getOfficeExtensionContext());
			assertNotNull("Should have Web architect", context.getWebArchitect());
			assertNotNull("Should have HTTP Security architect", context.getHttpSecurityArchitect());
			assertNotNull("Should have Procedure architect", context.getProcedureArchitect());
			assertNotNull("Should have Resource architect", context.getHttpResourceArchitect());
			assertNotNull("Should have Web templater", context.getWebTemplater());
			isRun.value = true;
		});
		try (MockWoofServer server = compiler.open()) {
			assertTrue("Should run WoOF extension", isRun.value);
		}
	}

	/**
	 * Ensure provide configuration through {@link CompileOfficeFloorExtension}.
	 */
	public void testOfficeFloorExtension() throws Exception {
		CompileWoof compiler = new CompileWoof();
		compiler.officeFloor((context) -> {
			Singleton.load(context.getOfficeFloorDeployer(), "OfficeFloor", context.getDeployedOffice());
		});
		compiler.web((context) -> {
			context.link(false, "/", ObjectLink.class);
		});
		try (MockWoofServer server = compiler.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest());
			response.assertJson(200, new Payload("OfficeFloor"));
		}
	}

	/**
	 * Ensure provide configuration through {@link CompileOfficeExtension}.
	 */
	public void testOfficeExtension() throws Exception {
		CompileWoof compiler = new CompileWoof();
		compiler.office((context) -> {
			Singleton.load(context.getOfficeArchitect(), "OfficeFloor");
		});
		compiler.web((context) -> {
			context.link(false, "/", ObjectLink.class);
		});
		try (MockWoofServer server = compiler.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest());
			response.assertJson(200, new Payload("OfficeFloor"));
		}
	}

	public static class ObjectLink {
		public void service(String value, ObjectResponse<Payload> response) {
			response.send(new Payload(value));
		}
	}

	@HttpObject
	public static class Payload {

		private String message;

		public Payload() {
		}

		public Payload(String message) {
			this.message = message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}
	}

	/**
	 * Ensure load.
	 */
	public void testLoad() throws Exception {
		new SystemPropertiesRule(WoOF.DEFAULT_OFFICE_PROFILES, "external").run(() -> {
			CompileWoof compiler = new CompileWoof(true);
			try (MockWoofServer server = compiler.open()) {
				MockWoofResponse response = server.send(MockWoofServer.mockRequest("/property"));
				response.assertResponse(200, "property to be overridden, to be overridden by profile, TEST_OVERRIDE");
			}
		});
	}

	/**
	 * Ensure not load configuration from project. This is for self contained
	 * testing.
	 */
	public void testNotLoad() throws Exception {
		new SystemPropertiesRule(WoOF.DEFAULT_OFFICE_PROFILES, "external").run(() -> {
			CompileWoof compiler = new CompileWoof();
			compiler.web((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();
				OfficeSection section = office.addOfficeSection("Property", MockPropertySectionSource.class.getName(),
						null);
				office.link(context.getWebArchitect().getHttpInput(false, "/property").getInput(),
						section.getOfficeSectionInput("service"));
			});
			try (MockWoofServer server = compiler.open()) {
				MockWoofResponse response = server.send(MockWoofServer.mockRequest("/property"));
				response.assertResponse(200,
						"property to be overridden, to be overridden by profile, to be overridden by test profile");
			}
		});
	}

}
