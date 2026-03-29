package net.officefloor.tutorial.inherithttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Ensure appropriately inherit content.
 * 
 * @author Daniel Sagenschneider
 */
public class InheritHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	/**
	 * Ensure able to obtain parent template.
	 */
	@Test
	public void testParent() throws IOException {
		this.doTest("parent", "parent-expected.html");
	}

	/**
	 * Ensure able to obtain child template.
	 */
	@Test
	public void testChild() throws IOException {
		this.doTest("child", "child-expected.html");
	}

	/**
	 * Ensure able to obtain grand child template.
	 */
	@Test
	public void testGrandChild() throws IOException {
		this.doTest("grandchild", "grandchild-expected.html");
	}

	/**
	 * Undertakes the {@link HttpRequest} and ensures the responding page is as
	 * expected.
	 * 
	 * @param url                               URL.
	 * @param fileNameContainingExpectedContent Name of file containing the expected
	 *                                          content.
	 */
	private void doTest(String url, String fileNameContainingExpectedContent) throws IOException {

		// Undertake the request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/" + url));
		assertEquals(200, response.getStatus().getStatusCode(), "Incorrect response status for URL " + url);
		String content = response.getEntity(null);

		// Obtain the expected content
		InputStream contentInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(fileNameContainingExpectedContent);
		assertNotNull(contentInputStream, "Can not find file " + fileNameContainingExpectedContent);
		Reader reader = new InputStreamReader(contentInputStream);
		StringWriter expected = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader.read()) {
			expected.append((char) character);
		}
		reader.close();

		// Ensure the context is as expected
		assertEquals(expected.toString(), content, "Incorrect content for URL " + url);
	}

}