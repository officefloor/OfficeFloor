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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorExtension;
import net.officefloor.frame.test.Closure;
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
public class CompileWoofTest {

	/**
	 * Ensure can link web.
	 */
	@Test
	public void webExtension() throws Exception {
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
	@Test
	public void woofExtension() throws Exception {
		CompileWoof compiler = new CompileWoof();
		Closure<Boolean> isRun = new Closure<>(false);
		compiler.woof((context) -> {
			assertNotNull(context.getOfficeArchitect(), "Should have Office architect");
			assertNotNull(context.getOfficeExtensionContext(), "Should have Office context");
			assertNotNull(context.getWebArchitect(), "Should have Web architect");
			assertNotNull(context.getHttpSecurityArchitect(), "Should have HTTP Security architect");
			assertNotNull(context.getProcedureArchitect(), "Should have Procedure architect");
			assertNotNull(context.getHttpResourceArchitect(), "Should have Resource architect");
			assertNotNull(context.getWebTemplater(), "Should have Web templater");
			isRun.value = true;
		});
		try (MockWoofServer server = compiler.open()) {
			assertTrue(isRun.value, "Should run WoOF extension");
		}
	}

	/**
	 * Ensure provide configuration through {@link CompileOfficeFloorExtension}.
	 */
	@Test
	public void officeFloorExtension() throws Exception {
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
	@Test
	public void officeExtension() throws Exception {
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
	@Test
	public void load() throws Exception {
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
	@Test
	public void notLoad() throws Exception {
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
