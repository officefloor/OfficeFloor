package net.officefloor.tutorial.springresttesting;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
/**
 * One integration test for what the unit tests cannot cover: the wiring.
 * The order of the functions, the @Valid binding, and the escalation routing
 * are framework behaviour, so they are proven through the running application.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ArticleEndpointIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ArticleRepository repository;

	@AfterEach
	public void clearData() {
		repository.deleteAll();
	}

	// The functions run in the order the YAML declares: validate, load, apply, save, respond.
	@Test
	public void update() throws Exception {
		Article saved = repository.save(new Article(null, "Before", "old"));

		mvc.perform(put("/article/" + saved.getId())
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("After", "new"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("After"));
	}

	// Validate runs before Load. An invalid body against a missing id is a 400,
	// not a 404. A unit test cannot show this: @Valid is applied by the framework,
	// not by calling the method.
	@Test
	public void invalidBodyIsRejectedBeforeTheLoad() throws Exception {
		mvc.perform(put("/article/99999")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("", "new"))))
				.andExpect(status().isBadRequest());
	}

	// A missing article escalates NotFoundException to the 404 handler.
	@Test
	public void missingArticleIsNotFound() throws Exception {
		mvc.perform(put("/article/99999")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("Valid", "body"))))
				.andExpect(status().isNotFound());
	}
}
// END SNIPPET: tutorial
