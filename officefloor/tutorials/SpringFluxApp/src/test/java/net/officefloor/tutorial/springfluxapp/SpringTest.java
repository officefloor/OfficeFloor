package net.officefloor.tutorial.springfluxapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Tests the Spring Web Flux application.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SpringTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void inject() {
		this.webTestClient.get().uri("/complex/inject").exchange().expectStatus().isOk().expectBody(String.class)
				.value((body) -> assertEquals(body, "Inject Dependency"));
	}

	@Test
	public void status() {
		this.webTestClient.get().uri("/complex/status").exchange().expectStatus().isCreated().expectBody(String.class)
				.value((body) -> assertEquals(body, "Status"));
	}

	@Test
	public void pathParam() {
		this.webTestClient.get().uri("/complex/path/value").exchange().expectStatus().isOk().expectBody(String.class)
				.value((body) -> assertEquals(body, "Parameter value"));
	}

	@Test
	public void queryParam() {
		this.webTestClient.get().uri("/complex/query?param=value").exchange().expectStatus().isOk()
				.expectBody(String.class).value((body) -> assertEquals(body, "Parameter value"));
	}

	@Test
	public void header() {
		this.webTestClient.get().uri("/complex/header").header("header", "value").exchange().expectStatus().isOk()
				.expectBody(String.class).value((body) -> assertEquals(body, "Header value"));
	}

	@Test
	public void post() {
		this.webTestClient.post().uri("/complex").bodyValue("value").exchange().expectStatus().isOk()
				.expectBody(String.class).value((body) -> assertEquals(body, "Body value"));
	}

}