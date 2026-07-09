package net.officefloor.tutorial.springrestcrud;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestCrudTest {

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

	// POST: Build -> Save -> RespondWithArticleCreated
	@Test
	public void createArticle() throws Exception {
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("My Title", "My content"))))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/article/")))
				.andExpect(jsonPath("$.title").value("My Title"))
				.andExpect(jsonPath("$.id").isNumber());
	}

	// Validation runs in Build before persistence: a blank title is a 400, not a saved row
	@Test
	public void createArticleInvalid() throws Exception {
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("", "content"))))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("title")));
	}

	// GET /article/{id}: Load -> RespondWithArticle
	@Test
	public void getArticleById() throws Exception {
		Article saved = repository.save(new Article(null, "Find Me", "content"));

		mvc.perform(get("/article/" + saved.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Find Me"));
	}

	// Load throws NotFoundException; the office-level escalation handler maps it to 404
	@Test
	public void getArticleNotFound() throws Exception {
		mvc.perform(get("/article/99999").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	// GET /article: single ListArticles step
	@Test
	public void listArticles() throws Exception {
		repository.save(new Article(null, "Article One", "content one"));
		repository.save(new Article(null, "Article Two", "content two"));

		mvc.perform(get("/article").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	// PUT: Validate -> Load -> Apply -> Save -> RespondWithArticle
	@Test
	public void updateArticle() throws Exception {
		Article saved = repository.save(new Article(null, "Before", "old"));

		mvc.perform(put("/article/" + saved.getId())
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("After", "new"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("After"))
				.andExpect(jsonPath("$.content").value("new"));

		// Verify the change was persisted, not just reflected in the response.
		Article persisted = repository.findById(saved.getId()).orElseThrow();
		assertEquals("After", persisted.getTitle());
		assertEquals("new", persisted.getContent());
	}

	// Validate runs BEFORE Load: an invalid body is a 400 even when the id does not exist
	@Test
	public void updateArticleInvalidBodyBeforeLoad() throws Exception {
		mvc.perform(put("/article/99999")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("", "new"))))
				.andExpect(status().isBadRequest());
	}

	// DELETE: Load -> Delete -> RespondWithNoContent
	@Test
	public void deleteArticle() throws Exception {
		Article saved = repository.save(new Article(null, "To Delete", "content"));

		mvc.perform(delete("/article/" + saved.getId()))
				.andExpect(status().isNoContent());

		mvc.perform(get("/article").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	public void deleteArticleNotFound() throws Exception {
		mvc.perform(delete("/article/99999"))
				.andExpect(status().isNotFound());
	}
}
// END SNIPPET: tutorial
