package net.officefloor.tutorial.javascriptapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests the JavaScript application.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@ExtendWith(OfficeFloorExtension.class)
public class JavaScriptAppTest {

	@RegisterExtension
	public HttpClientExtension client = new HttpClientExtension();

	@Test
	public void testHttpParameters() throws IOException {
		String response = this.doAjax("addition", "application/x-www-form-urlencoded", "numberOne=2&numberTwo=1");
		assertEquals("3", response, "Incorrect response");
	}

	@Test
	public void testHttpJson() throws IOException {
		String response = this.doAjax("subtraction", "application/json",
				"{ \"numberOne\" : \"3\", \"numberTwo\" : \"1\" }");
		assertEquals("{\"result\":\"2\"}", response, "Incorrect response");
	}

	private String doAjax(String link, String contentType, String payload) throws IOException {
		HttpPost post = new HttpPost("http://localhost:7878/template+" + link);
		post.addHeader("content-type", contentType);
		post.setEntity(new StringEntity(payload));
		HttpResponse response = this.client.execute(post);
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
		return entity;
	}
	// END SNIPPET: tutorial

}