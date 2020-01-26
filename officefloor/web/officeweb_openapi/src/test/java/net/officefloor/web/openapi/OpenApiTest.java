package net.officefloor.web.openapi;

import io.swagger.v3.core.util.Json;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test using OpenAPI.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenApiTest extends OfficeFrameTestCase {

	/**
	 * Ensure can generate JSON specification.
	 */
	public void testOpenApi() throws Exception {
		
		String result = Json.mapper().writeValueAsString(Parent.class);
		System.out.println("RESULT: " + result);
	}
	
	public static class Parent {
		
		public String getMessage() {
			return "test";
		}
	}
}