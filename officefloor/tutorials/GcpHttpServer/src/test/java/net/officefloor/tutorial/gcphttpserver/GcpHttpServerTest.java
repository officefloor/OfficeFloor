package net.officefloor.tutorial.gcphttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.tutorial.gcphttpserver.Logic.Message;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the GCP HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class GcpHttpServerTest extends GcpHttpServerIT {

	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void ensureGetDefaultResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/"));
		response.assertResponse(200, "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureGetResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/index.html"));
		response.assertResponse(200, "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureGetDefaultDirectoryResource() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/sub"));
		response.assertResponse(200, "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetDefaultDirectory() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/sub/"));
		response.assertResponse(200, "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetDirectory() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/sub/index.html"));
		response.assertResponse(200, "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureRestEndPoint() throws Exception {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/rest"));
		response.assertJson(200, new Message("Hello from GCP"));
	}

}