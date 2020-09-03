package net.officefloor.tutorial.exceptionhttpserver;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Ensure appropriately handling exception.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: handle
public class ExceptionHttpServerJUnit4Test {

	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule(this);

	@Rule
	public HttpClientRule client = new HttpClientRule();

	private PrintStream stderr;

	@Before
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
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());

		// Ensure handling by logging the failure
		String log = new String(error.toByteArray()).trim();
		assertEquals("Should log error", "Test", log);
	}

	@After
	public void reinstate() {
		// Reinstate stderr
		System.setErr(this.stderr);
	}
}
// END SNIPPET: handle
