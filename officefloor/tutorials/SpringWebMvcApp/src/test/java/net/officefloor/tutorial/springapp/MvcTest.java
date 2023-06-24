package net.officefloor.tutorial.springapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Tests over HTTP.
 * 
 * @author Daniel Sagenschneider
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MvcTest {

	@LocalServerPort
	private int port;

	private URL baseUrl;

	@Autowired
	private TestRestTemplate template;

	@BeforeEach
	public void loadUrl() throws Exception {
		this.baseUrl = new URL("http://localhost:" + this.port);
	}

	private String url(String path) {
		return this.baseUrl.toString() + path;
	}

	@Test
	public void getSimple() {
		ResponseEntity<String> response = this.template.getForEntity(this.url("/simple"), String.class);
		assertEquals(response.getBody(), "Simple Spring");
	}

	@Test
	public void getInject() {
		ResponseEntity<String> response = this.template.getForEntity(this.url("/complex/inject"), String.class);
		assertEquals(response.getBody(), "Inject Dependency");
	}

	@Test
	public void getStatus() {
		ResponseEntity<String> response = this.template.getForEntity(this.url("/complex/status"), String.class);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(response.getBody(), "Status");
	}

	@Test
	public void getPathParam() {
		ResponseEntity<String> response = this.template.getForEntity(this.url("/complex/path/value"), String.class);
		assertEquals(response.getBody(), "Parameter value");
	}

	@Test
	public void getQueryParam() {
		ResponseEntity<String> response = this.template.getForEntity(this.url("/complex/query?param=value"),
				String.class);
		assertEquals(response.getBody(), "Parameter value");
	}

	@Test
	public void getHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("header", "value");
		ResponseEntity<String> response = this.template.exchange(this.url("/complex/header"), HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		assertEquals(response.getBody(), "Header value");
	}

	@Test
	public void post() {
		ResponseEntity<String> response = this.template.exchange(this.url("/complex"), HttpMethod.POST,
				new HttpEntity<>("value"), String.class);
		assertEquals(response.getBody(), "Body value");
	}

}