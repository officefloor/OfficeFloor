package net.officefloor.tutorial.springapp;

import static org.junit.Assert.assertEquals;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;

/**
 * Ensure can run as WAR application.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringIT {

	@Test
	public void getSimple() throws Exception {
		this.doTest(this.get("/simple"), "Simple Spring");
	}

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

	private void doTest(ClassicHttpRequest request, String body) throws Exception {
		this.doTest(request, 200, body);
	}

	private void doTest(ClassicHttpRequest request, int status, String body) throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			try (CloseableHttpResponse response = client.execute(request)) {
				String entity = EntityUtils.toString(response.getEntity());
				assertEquals("Should be successful: " + entity, status, response.getCode());
				assertEquals("Incorrect entity", body, entity);
			}
		}
	}

}