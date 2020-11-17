package net.officefloor.tutorial.gcphttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;

/**
 * Tests the GCP HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class GcpHttpServerIT {

	// START SNIPPET: tutorial
	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension(false, 8080);

	@Test
	public void ensureGetResource() throws Exception {
		this.doTest("/index.html", "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureRestEndPoint() throws Exception {
		this.doTest("/rest", "{\"message\":\"Hello from GCP\"}");
	}

	private void doTest(String path, String entity) throws IOException {

		// Obtain the resource
		HttpGet get = new HttpGet(this.client.url(path));
		HttpResponse response = this.client.execute(get);

		// Ensure obtained resource
		String actualEntity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + actualEntity);
		assertEquals(entity, actualEntity);
	}
	// END SNIPPET: tutorial

	@Test
	public void ensureGetDefaultResource() throws Exception {
		this.doTest("/", "<html><body>Hello from GCP</body></html>");
	}

	@Test
	public void ensureGetDirectoryDefaultResource() throws Exception {
		this.doTest("/sub", "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetAsDirectoryDefaultResource() throws Exception {
		this.doTest("/sub/", "<html><body>Hello from GCP sub directory</body></html>");
	}

	@Test
	public void ensureGetDirectoryResource() throws Exception {
		this.doTest("/sub/index.html", "<html><body>Hello from GCP sub directory</body></html>");
	}

}