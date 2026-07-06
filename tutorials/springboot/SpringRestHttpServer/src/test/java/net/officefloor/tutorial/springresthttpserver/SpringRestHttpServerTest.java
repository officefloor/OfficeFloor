package net.officefloor.tutorial.springresthttpserver;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

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
	public void getFormalGreeting() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/greeting/formal/Alice")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Good day, Alice."))));
	}

	@Test
	public void getCasualGreeting() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/greeting/casual/Alice")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Hey, Alice!"))));
	}

	@Test
	public void getGreetingEntity() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/greeting/entity/OfficeFloor")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Greeting-Name", "OfficeFloor"))
				.andExpect(content().json(mapper.writeValueAsString(new GreetingResponse("Hello, OfficeFloor!"))));
	}

	@Test
	public void getStyledGreeting_bothVariablesExtracted() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/greeting/royal/Alice")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(
						new GreetingResponse("royal: Hello, Alice!"))));
	}

	@Test
	public void getStyledGreeting_literalSegmentWinsOverVariable() throws Exception {
		// /greeting/formal/Alice matches greeting/formal/{name}.GET.yml (literal "formal"),
		// NOT greeting/{style}/{name}.GET.yml — literal segments take priority over variables
		mvc.perform(MockMvcRequestBuilders.get("/greeting/formal/Alice")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(
						new GreetingResponse("Good day, Alice."))));
	}

}
// END SNIPPET: tutorial
