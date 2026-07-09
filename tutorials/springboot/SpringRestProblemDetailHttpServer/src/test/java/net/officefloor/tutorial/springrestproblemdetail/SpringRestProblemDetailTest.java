package net.officefloor.tutorial.springrestproblemdetail;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestProblemDetailTest {

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

	// Domain exception -> 404 Problem Detail (application/problem+json)
	@Test
	public void notFoundProblem() throws Exception {
		mvc.perform(get("/article/99999").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.title").value("Article not found"))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("No article exists with id 99999"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	// Framework validation exception -> 400 Problem Detail with field errors
	@Test
	public void validationProblem() throws Exception {
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("", "body"))))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.title").value("Request validation failed"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.title").value("title is required"));
	}

	// Anything else -> 500 Problem Detail, no internal leak
	@Test
	public void unexpectedErrorProblem() throws Exception {
		mvc.perform(get("/boom").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.title").value("Internal server error"))
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred while processing the request"));
	}

	// Happy path still works
	@Test
	public void createSucceeds() throws Exception {
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("Title", "body"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Title"));
	}
}
// END SNIPPET: tutorial
