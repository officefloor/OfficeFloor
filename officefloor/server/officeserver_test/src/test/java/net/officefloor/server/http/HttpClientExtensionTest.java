package net.officefloor.server.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Tests the {@link HttpClientExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpClientExtensionTest {

	/**
	 * {@link HttpClientExtension} to test.
	 */
	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension();

	/**
	 * Ensure can use {@link HttpClientRule} to interact with server.
	 */
	@Test
	public void testClient() throws Exception {

		// Compile OfficeFloor
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {
			new HttpServer(context.getDeployedOffice().getDeployedOfficeInput("SERVICE", "service"),
					context.getOfficeFloorDeployer(), context.getOfficeFloorSourceContext());
		});
		compiler.office((context) -> {
			context.addSection("SERVICE", MockServicer.class);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Ensure client can send request
			HttpResponse response = this.client.execute(new HttpGet(this.client.url("/")));
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");
			assertEquals("TEST", EntityUtils.toString(response.getEntity()), "Incorrect response");
		}
	}

	public static class MockServicer {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

}