package net.officefloor.tutorial.environmenthttpserver;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;
import net.officefloor.test.system.EnvironmentExtension;

/**
 * Tests with environment property.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentPropertyTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final EnvironmentExtension environment = new EnvironmentExtension()
			.property("OFFICEFLOOR.application.service.procedure.name", "ENVIRONMENT");

	@Order(2)
	@RegisterExtension
	public final OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	@Order(3)
	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension();

	@Test
	public void environment() throws IOException {
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:7878"));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect property override", "ENVIRONMENT", EntityUtils.toString(response.getEntity()));
	}
	// END SNIPPET: tutorial
}