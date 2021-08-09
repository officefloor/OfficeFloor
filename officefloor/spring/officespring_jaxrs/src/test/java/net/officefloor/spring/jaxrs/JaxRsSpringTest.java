/*-
 * #%L
 * JAX-RS with Spring Integration
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

package net.officefloor.spring.jaxrs;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests JAX-RS with Spring.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsSpringTest extends OfficeFrameTestCase {

	/**
	 * Ensure JAX-RS resource available.
	 */
	public void testJaxRs() throws Exception {
		this.doJaxRsSpringTest("/jaxrs", "JAX-RS");
	}

	/**
	 * Ensure JAX-RS resource available.
	 */
	public void testJaxRsAutowiredSpring() throws Exception {
		this.doJaxRsSpringTest("/jaxrs/autowired/spring", "Autowired Spring");
	}

	/**
	 * Ensure JAX-RS resource available.
	 */
	public void testJaxRsAutowiredOfficeFloor() throws Exception {
		this.doJaxRsSpringTest("/jaxrs/autowired/officefloor", "Autowired OfficeFloor");
	}

	/**
	 * Ensure JAX-RS resource available.
	 */
	public void testJaxRsInjectSpring() throws Exception {
		this.doJaxRsSpringTest("/jaxrs/inject/spring", "Inject Spring");
	}

	/**
	 * Ensure JAX-RS resource available.
	 */
	public void testJaxRsInjectOfficeFloor() throws Exception {
		this.doJaxRsSpringTest("/jaxrs/inject/officefloor", "Inject OfficeFloor");
	}

	/**
	 * Ensure JAX-RS resource available.
	 */
	public void testJaxRsDependencyOfficeFloor() throws Exception {
		this.doJaxRsSpringTest("/jaxrs/dependency", "Dependency OfficeFloor");
	}

	/**
	 * Ensure Spring resource available.
	 */
	public void testSpring() throws Exception {
		this.doJaxRsSpringTest("/spring", "SPRING");
	}

	/**
	 * Ensure Spring resource available.
	 */
	public void testSpringAutowiredSpring() throws Exception {
		this.doJaxRsSpringTest("/spring/autowired/spring", "Autowired Spring");
	}

	/**
	 * Ensure Spring resource available.
	 */
	public void testSpringAutowiredOfficeFloor() throws Exception {
		this.doJaxRsSpringTest("/spring/autowired/officefloor", "Autowired OfficeFloor");
	}

	/**
	 * Ensure Spring resource available.
	 */
	public void testSpringInjectSpring() throws Exception {
		this.doJaxRsSpringTest("/spring/inject/spring", "Inject Spring");
	}

	/**
	 * Ensure Spring resource available.
	 */
	public void testSpringInjectOfficeFloor() throws Exception {
		this.doJaxRsSpringTest("/spring/inject/officefloor", "Inject OfficeFloor");
	}

	/**
	 * Undertakes JAX-RS Spring test.
	 * 
	 * @param path           Path to JAX-RS resource.
	 * @param expectedEntity Expected entity in response.
	 */
	private void doJaxRsSpringTest(String path, String expectedEntity) throws Exception {
		CompileWoof compile = new CompileWoof(true);
		try (MockWoofServer server = compile.open(ServletWoofExtensionService.getChainServletsPropertyName("OFFICE"),
				"true")) {
			MockHttpResponse response = server.send(MockHttpServer.mockRequest(path));
			response.assertResponse(200, expectedEntity);
		}
	}

}
