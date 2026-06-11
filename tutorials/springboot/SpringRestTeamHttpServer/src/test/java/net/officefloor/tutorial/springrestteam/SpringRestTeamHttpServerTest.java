package net.officefloor.tutorial.springrestteam;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestTeamHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void threadDemoReturnsThreadNames() throws Exception {
		mvc.perform(get("/thread").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.socketThread").isString())
				.andExpect(jsonPath("$.databaseThread").isString())
				.andExpect(jsonPath("$.tableCount").isNumber());
	}

	@Test
	public void differentThreadsServiceEachStep() throws Exception {
		MvcResult result = mvc.perform(get("/thread").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		ThreadDemoResponse response = mapper.readValue(
				result.getResponse().getContentAsString(), ThreadDemoResponse.class);

		assertNotNull(response.getSocketThread(), "socketThread must be populated");
		assertNotNull(response.getDatabaseThread(), "databaseThread must be populated");

		// With team support active, the blocking database step runs on a dedicated
		// thread pool — socket threads are never blocked waiting on the database.
		assertNotEquals(response.getSocketThread(), response.getDatabaseThread(),
				"socket thread and database thread should differ once team support is active");
	}
}
// END SNIPPET: tutorial
