package net.officefloor.tutorial.exceptionhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Ensure appropriately handling exception.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: handle
@ExtendWith(OfficeFloorExtension.class)
public class ExceptionHttpServerTest {

	@RegisterExtension
	public HttpClientExtension client = new HttpClientExtension();

	private PrintStream stderr;

	@BeforeEach
	public void setup() {
		// Maintain stderr to reinstate
		this.stderr = System.err;
	}

	@Test
	public void ensureHandleException() throws Exception {

		// Override stderr
		ByteArrayOutputStream error = new ByteArrayOutputStream();
		System.setErr(new PrintStream(error, true));

		// Submit to trigger the exception
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/template+submit")));
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");

		// Ensure handling by logging the failure
		String log = new String(error.toByteArray()).trim();
		assertEquals("Test", log, "Should log error");
	}

	@AfterEach
	public void reinstate() {
		// Reinstate stderr
		System.setErr(this.stderr);
	}
}
// END SNIPPET: handle
