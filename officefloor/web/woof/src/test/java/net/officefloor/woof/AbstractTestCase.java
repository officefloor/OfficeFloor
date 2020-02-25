package net.officefloor.woof;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

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
	protected void doRequestTest(String path, String expectedEntity) throws IOException {

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open();

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
	 * @param systemPropertyName  {@link System} {@link Properties} name.
	 * @param systemPropertyValue {@link System} {@link Properties} value.
	 * @param path                Path.
	 * @param expectedEntity      Expected entity.
	 */
	protected void doSystemPropertiesTest(String systemPropertyName, String systemPropertyValue, String path,
			String expectedEntity) throws IOException {

		// Track original value, to reset
		String originalValue = System.getProperty(systemPropertyName);
		try {

			// Provide property override
			System.setProperty(systemPropertyName, systemPropertyValue);

			// Undertake request
			this.doRequestTest(path, expectedEntity);

		} finally {
			if (originalValue == null) {
				System.clearProperty(systemPropertyName);
			} else {
				System.setProperty(systemPropertyName, originalValue);
			}
		}
	}

}