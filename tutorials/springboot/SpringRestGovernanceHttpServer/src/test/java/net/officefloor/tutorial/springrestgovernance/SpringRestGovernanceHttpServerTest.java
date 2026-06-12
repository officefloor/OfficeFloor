package net.officefloor.tutorial.springrestgovernance;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestGovernanceHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void governedEndpointRunsGovernanceBeforeServiceBody() throws Exception {
		MvcResult result = mvc.perform(get("/governed").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		List<String> events = mapper.readValue(
				result.getResponse().getContentAsString(),
				new TypeReference<List<String>>() {});

		assertTrue(events.contains("governance-begin"),
				"Governance must call @Govern before function body runs");
		assertTrue(events.contains("service-called"),
				"Service body must execute");

		// Governance fires BEFORE the service method body
		assertTrue(events.indexOf("governance-begin") < events.indexOf("service-called"),
				"@Govern must precede the service method body");
	}

	@Test
	public void ungovernedEndpointSkipsGovernance() throws Exception {
		MvcResult result = mvc.perform(get("/ungoverned").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		List<String> events = mapper.readValue(
				result.getResponse().getContentAsString(),
				new TypeReference<List<String>>() {});

		assertFalse(events.contains("governance-begin"),
				"Governance must not run on a function with no govern: declaration");
		assertTrue(events.contains("service-called"),
				"Service body must still execute without governance");
	}
}
// END SNIPPET: tutorial
