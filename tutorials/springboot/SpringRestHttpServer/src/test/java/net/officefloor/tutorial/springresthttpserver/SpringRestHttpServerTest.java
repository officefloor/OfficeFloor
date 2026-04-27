package net.officefloor.tutorial.springresthttpserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private AuditService auditService;

	@Test
	public void getGreeting() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/greeting")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Hello, World!"))));
	}

	@Test
	public void getNamedGreeting() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/greeting/OfficeFloor")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Hello, OfficeFloor!"))));
	}

	@Test
	public void postGreeting() throws Exception {
		mvc.perform(post("/greeting")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new GreetingRequest("Daniel"))))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Hello, Daniel!"))));

		assertTrue(auditService.getEntries().contains("Hello, Daniel!"),
				"Greeting should be recorded by AuditService");
	}

	@Test
	public void postGreetingWithBlankName() throws Exception {
		mvc.perform(post("/greeting")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new GreetingRequest(""))))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Hello, World!"))));
	}
}
// END SNIPPET: tutorial
