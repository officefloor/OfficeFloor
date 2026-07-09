package net.officefloor.tutorial.springrestresolve;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class ArticleResolveIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private TagRepository tagRepository;

	@AfterEach
	public void clearArticles() {
		articleRepository.deleteAll();
	}

	// Create resolves the tag names to the pre-seeded managed tags
	@Test
	public void createResolvesTags() throws Exception {
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("Guide", List.of("java", "spring")))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tags[0]").value("java"))
				.andExpect(jsonPath("$.tags[1]").value("spring"));
	}

	// The payoff: resolving reuses the four seeded tags. No new tag rows are created.
	@Test
	public void resolveReusesTagsAndDoesNotDuplicate() throws Exception {
		long tagsBefore = tagRepository.count();

		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("One", List.of("java", "spring")))))
				.andExpect(status().isCreated());
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("Two", List.of("java", "testing")))))
				.andExpect(status().isCreated());

		assertEquals(tagsBefore, tagRepository.count(), "resolving must not create new tag rows");
	}

	// The same ResolveTags runs on update. Verify through the repository that the
	// update ran and reused the managed tags rather than creating new ones.
	@Test
	public void updateReResolvesTags() throws Exception {
		long tagsBefore = tagRepository.count();
		Article saved = articleRepository.save(new Article(null, "Before"));

		mvc.perform(put("/article/" + saved.getId())
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("After", List.of("officefloor")))))
				.andExpect(status().isOk());

		assertEquals("After", articleRepository.findById(saved.getId()).orElseThrow().getTitle());
		assertEquals(tagsBefore, tagRepository.count(), "update must reuse managed tags, not create new");
	}

	@Test
	public void updateMissingArticleIsNotFound() throws Exception {
		mvc.perform(put("/article/99999")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("X", List.of("java")))))
				.andExpect(status().isNotFound());
	}

	@Test
	public void createInvalidIsBadRequest() throws Exception {
		mvc.perform(post("/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("", List.of("java")))))
				.andExpect(status().isBadRequest());
	}
}
// END SNIPPET: tutorial
