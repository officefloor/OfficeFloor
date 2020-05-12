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
	 * Ensure Spring resource available.
	 */
	public void testSpring() throws Exception {
		this.doJaxRsSpringTest("/spring", "SPRING");
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