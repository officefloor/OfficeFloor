package ${package};

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the application running within JEE Container and ensures a page is
 * available.
 */
public class RunApplicationIT {

	@Test
	public void ensureApplicationAvailable() throws Exception {

		// Connect to application and obtain page
		URL url = new URL("http://localhost:18080/form.woof");
		Reader response = new InputStreamReader(url.openStream());
		StringWriter content = new StringWriter();
		for (int character = response.read(); character != -1; character = response
				.read()) {
			content.write(character);
		}

		// Ensure correct page
		Assert.assertTrue("Incorrect page",
				content.toString().contains("<title>Page with form submission</title>"));
	}

}