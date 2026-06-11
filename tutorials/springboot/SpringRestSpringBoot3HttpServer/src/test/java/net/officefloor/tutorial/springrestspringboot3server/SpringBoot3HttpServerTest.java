package net.officefloor.tutorial.springrestspringboot3server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringBoot3HttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void getGreeting() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/greeting")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Hello from Spring Boot 3, World!"))));
	}
}
// END SNIPPET: tutorial
