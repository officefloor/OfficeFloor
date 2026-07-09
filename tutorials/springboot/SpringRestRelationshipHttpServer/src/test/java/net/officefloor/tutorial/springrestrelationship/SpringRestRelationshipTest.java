package net.officefloor.tutorial.springrestrelationship;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestRelationshipTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private ArticleRepository articleRepository;

	@AfterEach
	public void clearData() {
		articleRepository.deleteAll();
		authorRepository.deleteAll();
	}

	private Author saveAuthor(String name) {
		return authorRepository.save(new Author(null, name));
	}

	private Article saveArticle(Author author, String title) {
		Article article = new Article(null, title, "content");
		article.setAuthor(author);
		return articleRepository.save(article);
	}

	// Shared load: LoadAuthor -> RespondWithAuthor
	@Test
	public void getAuthor() throws Exception {
		Author ada = saveAuthor("Ada");
		mvc.perform(get("/author/" + ada.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Ada"));
	}

	@Test
	public void getAuthorNotFound() throws Exception {
		mvc.perform(get("/author/99999").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	// Global load: LoadArticle finds any article by id
	@Test
	public void getArticleGlobally() throws Exception {
		Article article = saveArticle(saveAuthor("Ada"), "Analytical Engine");
		mvc.perform(get("/article/" + article.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Analytical Engine"));
	}

	// Scoped load: the article is found within its own author
	@Test
	public void getAuthorsArticle() throws Exception {
		Author ada = saveAuthor("Ada");
		Article article = saveArticle(ada, "Analytical Engine");
		mvc.perform(get("/author/" + ada.getId() + "/article/" + article.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Analytical Engine"))
				.andExpect(jsonPath("$.authorId").value(ada.getId()));
	}

	// The ownership-scoping trap: an article of ANOTHER author is not found here,
	// even though it exists globally. A global load would leak it.
	@Test
	public void authorsArticleIsScopedToTheAuthor() throws Exception {
		Author ada = saveAuthor("Ada");
		Author grace = saveAuthor("Grace");
		Article gracesArticle = saveArticle(grace, "Compiler");

		// exists globally
		mvc.perform(get("/article/" + gracesArticle.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// but is NOT Ada's article
		mvc.perform(get("/author/" + ada.getId() + "/article/" + gracesArticle.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());

		// and IS Grace's article
		mvc.perform(get("/author/" + grace.getId() + "/article/" + gracesArticle.getId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	// Create under a parent, reusing LoadAuthor
	@Test
	public void createArticleForAuthor() throws Exception {
		Author ada = saveAuthor("Ada");
		mvc.perform(post("/author/" + ada.getId() + "/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("Notes", "on the engine"))))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/author/" + ada.getId() + "/article/")))
				.andExpect(jsonPath("$.title").value("Notes"))
				.andExpect(jsonPath("$.authorId").value(ada.getId()));
	}

	@Test
	public void createArticleForMissingAuthor() throws Exception {
		mvc.perform(post("/author/99999/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("Notes", "content"))))
				.andExpect(status().isNotFound());
	}

	// Validation runs before the author is loaded
	@Test
	public void createArticleInvalid() throws Exception {
		mvc.perform(post("/author/99999/article")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ArticleRequest("", "content"))))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("title")));
	}
}
// END SNIPPET: tutorial
