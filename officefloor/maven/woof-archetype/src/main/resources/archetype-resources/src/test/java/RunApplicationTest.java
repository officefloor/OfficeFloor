package ${package};

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Runs application and ensures a page is available.
 */
public class RunApplicationTest {

	@Before
	public void runApplication() throws Exception {
		// Start the application
		WoofOfficeFloorSource.start();
	}

	@Test
	public void ensureApplicationAvailable() throws Exception {

		// Connect to application and obtain page
		URL url = new URL("http://localhost:7878/static.woof");
		Reader response = new InputStreamReader(url.openStream());
		StringWriter content = new StringWriter();
		for (int character = response.read(); character != -1; character = response
				.read()) {
			content.write(character);
		}

		// Ensure correct page
		Assert.assertTrue("Incorrect page",
				content.toString().contains("<title>Static Page</title>"));
	}

	@After
	public void stopApplication() throws Exception {
		// Stop the application
		WoofOfficeFloorSource.stop();
	}

}