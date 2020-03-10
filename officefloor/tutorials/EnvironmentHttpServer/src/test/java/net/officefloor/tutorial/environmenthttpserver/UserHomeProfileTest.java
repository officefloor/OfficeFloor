package net.officefloor.tutorial.environmenthttpserver;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import net.officefloor.compile.test.system.SystemPropertiesRule;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;
import net.officefloor.woof.WoOF;

/**
 * Tests with <code>user.home</code> profile properties.
 * 
 * @author Daniel Sagenschneider
 */
public class UserHomeProfileTest {

	// START SNIPPET: tutorial
	private final SystemPropertiesRule systemProperties = new SystemPropertiesRule()
			.property("user.home", new File("./target/test-classes").getAbsolutePath())
			.property(WoOF.DEFAULT_OFFICE_PROFILES, "user");

	private final OfficeFloorRule officeFloor = new OfficeFloorRule();

	private final HttpClientRule client = new HttpClientRule();

	@Rule
	public final RuleChain order = RuleChain.outerRule(this.systemProperties).around(this.officeFloor)
			.around(this.client);

	@Test
	public void userHome() throws IOException {
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:7878"));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect property override", "USER_PROFILE", EntityUtils.toString(response.getEntity()));
	}
	// END SNIPPET: tutorial
}