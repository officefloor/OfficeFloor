package net.officefloor.tutorial.springrestdatajpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestDataJpaTest {

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

	@Test
	public void createArticle() throws Exception {
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("My Title", "My content"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("My Title"))
				.andExpect(jsonPath("$.content").value("My content"))
				.andExpect(jsonPath("$.id").isNumber());
	}

	@Test
	public void getArticleById() throws Exception {
		Article saved = repository.save(new Article(null, "Find Me", "content"));

		mvc.perform(get("/article/" + saved.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Find Me"));
	}

	@Test
	public void listArticles() throws Exception {
		repository.save(new Article(null, "Article One", "content one"));
		repository.save(new Article(null, "Article Two", "content two"));

		mvc.perform(get("/article").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].title").value("Article One"));
	}

	@Test
	public void deleteArticle() throws Exception {
		Article saved = repository.save(new Article(null, "To Delete", "content"));

		mvc.perform(delete("/article/" + saved.getId()))
				.andExpect(status().isNoContent());

		mvc.perform(get("/article").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	// Exception handler under govern=[transaction] — participates in the same transaction
	@Test
	public void deleteArticleNotFound() throws Exception {
		mvc.perform(delete("/article/99999"))
				.andExpect(status().isNotFound())
				.andExpect(content().string(containsString("not found")));
	}

	// Multi-step transaction: deleteAll and create share the same transaction
	@Test
	public void replaceAllArticles() throws Exception {
		repository.save(new Article(null, "Old One", "old content"));
		repository.save(new Article(null, "Old Two", "old content"));

		mvc.perform(post("/article/replace-all")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("New Article", "new content"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("New Article"));

		mvc.perform(get("/article").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title").value("New Article"));
	}
}
// END SNIPPET: tutorial
