package net.officefloor.tutorial.springresthttpserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// START SNIPPET: tutorial
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringRestHttpServerRealServerTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private AuditService auditService;

	@Test
	public void getGreeting() {
		ResponseEntity<GreetingResponse> response = restTemplate.getForEntity(
				"/greeting", GreetingResponse.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new GreetingResponse("Hello, World!"), response.getBody());
	}

	@Test
	public void getNamedGreeting() {
		ResponseEntity<GreetingResponse> response = restTemplate.getForEntity(
				"/greeting/OfficeFloor", GreetingResponse.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new GreetingResponse("Hello, OfficeFloor!"), response.getBody());
	}

	@Test
	public void postGreeting() {
		ResponseEntity<GreetingResponse> response = restTemplate.postForEntity(
				"/greeting", new GreetingRequest("Daniel"), GreetingResponse.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new GreetingResponse("Hello, Daniel!"), response.getBody());
		assertTrue(auditService.getEntries().contains("Hello, Daniel!"),
				"Greeting should be recorded by AuditService");
	}

	@Test
	public void postGreetingWithBlankName() {
		ResponseEntity<GreetingResponse> response = restTemplate.postForEntity(
				"/greeting", new GreetingRequest(""), GreetingResponse.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new GreetingResponse("Hello, World!"), response.getBody());
	}

	@Test
	public void getFormalGreeting() {
		ResponseEntity<GreetingResponse> response = restTemplate.getForEntity(
				"/greeting/formal/Alice", GreetingResponse.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new GreetingResponse("Good day, Alice."), response.getBody());
	}

	@Test
	public void getCasualGreeting() {
		ResponseEntity<GreetingResponse> response = restTemplate.getForEntity(
				"/greeting/casual/Alice", GreetingResponse.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new GreetingResponse("Hey, Alice!"), response.getBody());
	}

	@Test
	public void getGreetingEntity() {
		ResponseEntity<GreetingResponse> response = restTemplate.getForEntity(
				"/greeting/entity/OfficeFloor", GreetingResponse.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("OfficeFloor", response.getHeaders().getFirst("X-Greeting-Name"));
		assertEquals(new GreetingResponse("Hello, OfficeFloor!"), response.getBody());
	}
}
// END SNIPPET: tutorial
