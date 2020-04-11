package net.officefloor.tutorial.springapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
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

	@Test
	public void getSimple() {
		ResponseEntity<String> response = this.template.getForEntity(this.baseUrl.toString() + "/simple", String.class);
		assertEquals(response.getBody(), "Simple Spring");
	}
}