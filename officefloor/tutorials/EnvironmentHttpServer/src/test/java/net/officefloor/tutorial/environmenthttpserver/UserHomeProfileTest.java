package net.officefloor.tutorial.environmenthttpserver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;
import net.officefloor.test.system.SystemPropertiesExtension;
import net.officefloor.woof.WoOF;

/**
 * Tests with <code>user.home</code> profile properties.
 * 
 * @author Daniel Sagenschneider
 */
public class UserHomeProfileTest {

	// START SNIPPET: tutorial
	@Order(1)
	@RegisterExtension
	public final SystemPropertiesExtension systemProperties = new SystemPropertiesExtension()
			.property("user.home", new File("./target/test-classes").getAbsolutePath())
			.property(WoOF.DEFAULT_OFFICE_PROFILES, "user");

	@Order(2)
	@RegisterExtension
	public final OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	@Order(3)
	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension();

	@Test
	public void userHome() throws IOException {
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:7878"));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect property override", "USER_PROFILE", EntityUtils.toString(response.getEntity()));
	}
	// END SNIPPET: tutorial
}