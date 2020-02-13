package net.officefloor.web.openapi;

import java.util.function.Consumer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can send Swagger UI resources for viewing Open API information.
 * 
 * @author Daniel Sagenschneider
 */
public class SwaggerUiTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to obtain <code>index.html</code>.
	 */
	public void testSwagger() throws Exception {
		this.doIndexTest("/swagger");
	}

	/**
	 * Ensure intercept <code>index.html</code>.
	 */
	public void testInterceptIndexHtml() throws Exception {
		this.doIndexTest("/swagger/index.html");
	}

	/**
	 * Ensure able to obtain backing files.
	 */
	public void testRemainingFiles() throws Exception {
		for (String path : new String[] { "absolute-path.js", "index.js", "oauth2-redirect.html",
				"swagger-ui-bundle.js", "swagger-ui-standalone-preset.js", "swagger-ui.css", "swagger-ui.js" }) {
			this.doSwaggerTest("/swagger/" + path, null);
		}
	}

	/**
	 * Undertakes test for <code>index.html</code>.
	 * 
	 * @param path Path.
	 */
	private void doIndexTest(String path) throws Exception {
		this.doSwaggerTest(path, (response) -> {
			assertEquals("Incorrect Content-Type", "text/html", response.getHeader("content-type").getValue());
			String entity = response.getEntity(null);
			assertTrue("Incorrect index.html", entity.contains("<title>Swagger UI</title>"));
			assertFalse("Should not contain petstore URL", entity.contains("petstore"));
			assertTrue("Should contain hosted OpenApi URL", entity.contains("\"/openapi.json\""));
		});
	}

	/**
	 * Undertakes swagger request.
	 * 
	 * @param path      Path for swagger file.
	 * @param validator Validates the swagger file.
	 */
	private void doSwaggerTest(String path, Consumer<MockHttpResponse> validator) throws Exception {
		CompileWoof compiler = new CompileWoof();
		compiler.web((context) -> {
			context.link(false, "/path", NoOperationSection.class);
		});
		try (MockWoofServer server = compiler.open()) {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/swagger"));
			assertEquals("Should be successful (path: " + path + ")", 200, response.getStatus().getStatusCode());
			if (validator != null) {
				validator.accept(response);
			}
		}
	}

	public static class NoOperationSection {
		public void service() {
			// no operation
		}
	}

}