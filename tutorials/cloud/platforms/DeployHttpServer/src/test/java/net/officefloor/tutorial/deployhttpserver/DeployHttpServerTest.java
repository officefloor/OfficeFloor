package net.officefloor.tutorial.deployhttpserver;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the deployed HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class DeployHttpServerTest {

	@Rule
	public OfficeFloorRule officefloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void ensureRunning() throws IOException {
		HttpResponse response = this.client.execute(new HttpGet(this.client.url("/deploy")));
		assertEquals("Should be succesful", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect response", "<html><body>Deployed</body></html>",
				EntityUtils.toString(response.getEntity()));
	}

}