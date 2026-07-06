package net.officefloor.tutorial.springrestsupplier;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestSupplierHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void publishedMessageIsReceivedBySubscriber() throws Exception {
		// Publish returns 204 (No Content) — correct for a fire-and-forget operation
		mvc.perform(post("/message")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Message("hello from supplier"))))
				.andExpect(status().is2xxSuccessful());

		// Subscriber reads from the same shared queue held by the supplier
		mvc.perform(get("/message"))
				.andExpect(status().isOk())
				.andExpect(content().string("hello from supplier"));
	}

	@Test
	public void multipleMessagesAreQueuedInOrder() throws Exception {
		mvc.perform(post("/message")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Message("first"))))
				.andExpect(status().is2xxSuccessful());
		mvc.perform(post("/message")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Message("second"))))
				.andExpect(status().is2xxSuccessful());

		// Queue preserves FIFO insertion order across requests
		mvc.perform(get("/message"))
				.andExpect(status().isOk())
				.andExpect(content().string("first"));
		mvc.perform(get("/message"))
				.andExpect(status().isOk())
				.andExpect(content().string("second"));

		// Queue is now drained — next receive returns empty
		mvc.perform(get("/message"))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
	}
}
// END SNIPPET: tutorial
