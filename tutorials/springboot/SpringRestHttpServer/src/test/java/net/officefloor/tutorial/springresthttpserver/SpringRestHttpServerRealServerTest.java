package net.officefloor.tutorial.springresthttpserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

// START SNIPPET: tutorial
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringRestHttpServerRealServerTest {

	@Autowired
	private TestRestTemplate restTemplate;

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
