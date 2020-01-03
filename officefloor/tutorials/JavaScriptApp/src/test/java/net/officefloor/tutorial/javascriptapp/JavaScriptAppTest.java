package net.officefloor.tutorial.javascriptapp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the JavaScript application.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaScriptAppTest {

	/**
	 * Run application.
	 */
	public static void main(String[] arguments) throws Exception {
		OfficeFloorMain.main(arguments);
	}

	// START SNIPPET: tutorial
	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void testHttpParameters() throws IOException {
		String response = this.doAjax("addition", "application/x-www-form-urlencoded", "numberOne=2&numberTwo=1");
		assertEquals("Incorrect response", "3", response);
	}

	@Test
	public void testHttpJson() throws IOException {
		String response = this.doAjax("subtraction", "application/json",
				"{ \"numberOne\" : \"3\", \"numberTwo\" : \"1\" }");
		assertEquals("Incorrect response", "{\"result\":\"2\"}", response);
	}

	private String doAjax(String link, String contentType, String payload) throws IOException {
		HttpPost post = new HttpPost("http://localhost:7878/template+" + link);
		post.addHeader("content-type", contentType);
		post.setEntity(new StringEntity(payload));
		HttpResponse response = this.client.execute(post);
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals("Should be successful: " + entity, 200, response.getStatusLine().getStatusCode());
		return entity;
	}
	// END SNIPPET: tutorial

}