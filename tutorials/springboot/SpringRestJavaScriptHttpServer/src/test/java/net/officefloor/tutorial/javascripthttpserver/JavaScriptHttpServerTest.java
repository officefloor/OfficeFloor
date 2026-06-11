package net.officefloor.tutorial.javascripthttpserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class JavaScriptHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void invalidIdentifier() throws Exception {
		mvc.perform(get("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Request(-1, "Daniel"))))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Invalid identifier"));
	}

	@Test
	public void invalidName() throws Exception {
		mvc.perform(get("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Request(1, ""))))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Must provide name"));
	}

	@Test
	public void validRequest() throws Exception {
		mvc.perform(get("/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Request(1, "Daniel")))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(mapper.writeValueAsString(new Response("successful"))));
	}
}
// END SNIPPET: tutorial
