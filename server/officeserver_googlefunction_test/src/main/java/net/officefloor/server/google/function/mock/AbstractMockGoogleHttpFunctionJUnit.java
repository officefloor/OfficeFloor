/*-
 * #%L
 * Google Function Testing
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.server.google.function.mock;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.google.function.wrap.AbstractSetupGoogleHttpFunctionJUnit;
import net.officefloor.server.google.function.wrap.HttpFunctionSectionSource;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Abstract Google {@link HttpFunction} JUnit functionality serviced by
 * {@link MockHttpServer}.
 */
public class AbstractMockGoogleHttpFunctionJUnit<J extends AbstractMockGoogleHttpFunctionJUnit<J>>
		extends AbstractSetupGoogleHttpFunctionJUnit<J> {

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer mockHttpServer;

	/**
	 * Instantiate.
	 * 
	 * @param httpFunctionClass {@link HttpFunction} {@link Class}.
	 */
	public AbstractMockGoogleHttpFunctionJUnit(Class<?> httpFunctionClass) {
		super(httpFunctionClass);
	}

	/**
	 * Instantiate using default {@link OfficeFloor} {@link HttpFunction}.
	 */
	public AbstractMockGoogleHttpFunctionJUnit() {
	}

	/**
	 * Obtains the {@link MockHttpServer}.
	 * 
	 * @return {@link MockHttpServer}.
	 */
	public MockHttpServer getMockHttpServer() {

		// Ensure within test
		JUnitAgnosticAssert.assertNotNull(this.mockHttpServer,
				OfficeFloor.class.getSimpleName() + " not running or not within test context");

		// Return the Mock HTTP Server
		return this.mockHttpServer;
	}

	/**
	 * Convenience method to send the {@link MockHttpRequestBuilder}.
	 * 
	 * @param request {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpResponse}.
	 */
	public MockHttpResponse send(MockHttpRequestBuilder request) {
		return this.getMockHttpServer().send(request);
	}

	/**
	 * Open the {@link MockHttpServer} for the {@link HttpFunction}.
	 * 
	 * @throws Exception If fails to start {@link MockHttpServer}.
	 */
	protected void openMockHttpServer() throws Exception {

		// Configure the mock HTTP server
		this.setupHttpFunction((deployer, context) -> {

			// Configure server to service requests
			DeployedOffice office = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
			DeployedOfficeInput input = office.getDeployedOfficeInput(HttpFunctionSectionSource.SECTION_NAME,
					HttpFunctionSectionSource.INPUT_NAME);
			AbstractMockGoogleHttpFunctionJUnit.this.mockHttpServer = MockHttpServer.configureMockHttpServer(input,
					HttpFunctionSectionSource.CONNECTION_TYPE_QUALIFIER);
		});
	}

}
