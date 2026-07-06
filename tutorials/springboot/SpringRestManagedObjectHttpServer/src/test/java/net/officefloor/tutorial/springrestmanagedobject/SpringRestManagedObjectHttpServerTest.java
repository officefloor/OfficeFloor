package net.officefloor.tutorial.springrestmanagedobject;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestManagedObjectHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	// --- class: (ClassManagedObjectSource) example ---

	@Test
	public void requestContextIsPopulated() throws Exception {
		mvc.perform(get("/request").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correlationId").isString())
				.andExpect(jsonPath("$.startTimeMs").isNumber());
	}

	@Test
	public void eachRequestGetsFreshManagedObjectInstance() throws Exception {
		MvcResult first = mvc.perform(get("/request").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		MvcResult second = mvc.perform(get("/request").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		RequestContextResponse r1 = mapper.readValue(
				first.getResponse().getContentAsString(), RequestContextResponse.class);
		RequestContextResponse r2 = mapper.readValue(
				second.getResponse().getContentAsString(), RequestContextResponse.class);

		assertNotNull(r1.getCorrelationId(), "correlationId must be present");
		assertNotNull(r2.getCorrelationId(), "correlationId must be present");

		// PROCESS scope: each incoming request gets a new managed object instance,
		// so the correlation IDs generated at construction time must differ.
		assertNotEquals(r1.getCorrelationId(), r2.getCorrelationId(),
				"PROCESS-scoped managed object must be a new instance per request");
	}

	@Test
	public void startTimeIsReasonable() throws Exception {
		long before = System.currentTimeMillis();
		MvcResult result = mvc.perform(get("/request").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		long after = System.currentTimeMillis();

		RequestContextResponse response = mapper.readValue(
				result.getResponse().getContentAsString(), RequestContextResponse.class);

		assertTrue(response.getStartTimeMs() >= before,
				"startTimeMs must be at or after the request was sent");
		assertTrue(response.getStartTimeMs() <= after,
				"startTimeMs must be at or before the response was received");
	}

	// --- source: (custom ManagedObjectSource) example ---

	@Test
	public void customManagedObjectSourceIsInjected() throws Exception {
		mvc.perform(get("/session"))
				.andExpect(status().isOk());
	}

	@Test
	public void customManagedObjectSourceProvidesUniqueInstancePerRequest() throws Exception {
		String id1 = mvc.perform(get("/session"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String id2 = mvc.perform(get("/session"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		assertNotNull(id1, "session ID must be present");
		assertNotNull(id2, "session ID must be present");

		// PROCESS scope: SessionIdSource creates a new SessionId per request,
		// so two successive requests must carry different IDs.
		assertNotEquals(id1, id2,
				"Custom ManagedObjectSource with PROCESS scope must provide a new instance per request");
	}
}
// END SNIPPET: tutorial
