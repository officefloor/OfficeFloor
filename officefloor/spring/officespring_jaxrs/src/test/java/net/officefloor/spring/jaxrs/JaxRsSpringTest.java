/*-
 * #%L
 * JAX-RS with Spring Integration
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
