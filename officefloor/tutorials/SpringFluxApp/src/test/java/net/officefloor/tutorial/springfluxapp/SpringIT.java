package net.officefloor.tutorial.springfluxapp;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

/**
 * Ensure can run as WAR application.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringIT {

	@Test
	public void getInject() throws Exception {
		this.doTest(this.get("/complex/inject"), "Inject Dependency");
	}

	@Test
	public void getStatus() throws Exception {
		this.doTest(this.get("/complex/status"), 201, "Status");
	}

	@Test
	public void getPathParam() throws Exception {
		this.doTest(this.get("/complex/path/value"), "Parameter value");
	}

	@Test
	public void getQueryParam() throws Exception {
		this.doTest(this.get("/complex/query?param=value"), "Parameter value");
	}

	@Test
	public void getHeader() throws Exception {
		HttpGet get = this.get("/complex/header");
		get.setHeader("header", "value");
		this.doTest(get, "Header value");
	}

	@Test
	public void post() throws Exception {
		HttpPost post = new HttpPost(this.url("/complex"));
		post.setEntity(new StringEntity("value"));
		this.doTest(post, "Body value");
	}

	private String url(String path) {
		return "http://localhost:8081" + path;
	}

	private HttpGet get(String path) {
		return new HttpGet(this.url(path));
	}

	private void doTest(HttpUriRequest request, String body) throws Exception {
		this.doTest(request, 200, body);
	}

	private void doTest(HttpUriRequest request, int status, String body) throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpResponse response = client.execute(request);
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Should be successful: " + entity, status, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect entity", body, entity);
		}
	}

}