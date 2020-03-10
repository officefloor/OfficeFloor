package net.officefloor.tutorial.environmenthttpserver;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import net.officefloor.compile.test.system.EnvironmentRule;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests with environment profile.
 * 
 * @author Daniel Sagenschneider
 */
public class EnvironmentProfileTest {

	private final EnvironmentRule environment = new EnvironmentRule().property("OFFICEFLOOR.application.profiles",
			"environment");

	private final OfficeFloorRule officeFloor = new OfficeFloorRule();

	private final HttpClientRule client = new HttpClientRule();

	@Rule
	public final RuleChain order = RuleChain.outerRule(this.environment).around(this.officeFloor).around(this.client);

	@Test
	public void environment() throws IOException {
		HttpResponse response = this.client.execute(new HttpGet("http://localhost:7878"));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect property override", "ENVIRONMENT", EntityUtils.toString(response.getEntity()));
	}
}