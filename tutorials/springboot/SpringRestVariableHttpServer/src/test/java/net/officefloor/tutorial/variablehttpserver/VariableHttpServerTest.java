package net.officefloor.tutorial.variablehttpserver;

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
public class VariableHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	private static final ServerResponse EXPECTED =
			new ServerResponse(new Person("Daniel", "Sagenschneider"), "Need to watch his code!");

	@Test
	public void outIn() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/outIn")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(EXPECTED)));
	}

	@Test
	public void varVal() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/varVal")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(EXPECTED)));
	}

	@Test
	public void outVal() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/outVal")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(EXPECTED)));
	}
}
// END SNIPPET: tutorial
