package net.officefloor.woof;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.compile.test.system.EnvironmentRule;
import net.officefloor.compile.test.system.SystemPropertiesRule;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpClientTestUtil;

/**
 * Abstract test case.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTestCase extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	protected OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Undertakes request.
	 * 
	 * @param path           Path.
	 * @param expectedEntity Expected entity.
	 */
	protected void doRequestTest(String path, String expectedEntity, String... commandLineProperties)
			throws IOException {

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open(commandLineProperties);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/" + path));
			assertEquals("Incorrect entity", expectedEntity, HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Undertakes properties test with {@link System} {@link Properties}.
	 * 
	 * @param path                         Path.
	 * @param expectedEntity               Expected entity.
	 * @param systemPropertyNameValuePairs {@link System} {@link Properties}
	 *                                     name/value pairs.
	 */
	protected void doSystemPropertiesTest(String path, String expectedEntity, String... systemPropertyNameValuePairs)
			throws IOException {
		new SystemPropertiesRule(systemPropertyNameValuePairs).run(() -> {
			this.doRequestTest(path, expectedEntity);
		});
	}

	/**
	 * Undertakes environment test.
	 * 
	 * @param path                      Path.
	 * @param expectedEntity            Expected entity.
	 * @param environmentNameValuePairs Environment name/value pairs.
	 */
	protected void doEnvironmentTest(String path, String expectedEntity, String... environmentNameValuePairs)
			throws Exception {
		new EnvironmentRule(environmentNameValuePairs).run(() -> {
			this.doRequestTest(path, expectedEntity);
		});
	}

}