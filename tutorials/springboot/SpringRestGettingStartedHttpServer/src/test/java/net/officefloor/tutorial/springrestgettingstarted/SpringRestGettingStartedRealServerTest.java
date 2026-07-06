package net.officefloor.tutorial.springrestgettingstarted;

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
public class SpringRestGettingStartedRealServerTest {

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
}
// END SNIPPET: tutorial
