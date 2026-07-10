package net.officefloor.tutorial.springresttesting;

import net.officefloor.model.test.variable.MockVar;
import net.officefloor.woof.mock.MockObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// START SNIPPET: tutorial
public class ArticleFunctionUnitTest {

	// A pure function: no dependencies at all. Just logic over two values.
	@Test
	public void applyArticle_copiesFieldsOntoTheEntity() {
		Article article = new Article(1L, "old", "old body");
		ArticleRequest request = new ArticleRequest("new", "new body");

		new ApplyArticle().service(article, request);

		assertEquals("new", article.getTitle());
		assertEquals("new body", article.getContent());
	}

	// A responder: capture what it sends with the framework's MockObjectResponse.
	@Test
	public void respondWithArticle_mapsEntityToDto() {
		Article article = new Article(7L, "Title", "Body");
		MockObjectResponse<ResponseEntity<ArticleResponse>> response = new MockObjectResponse<>();

		new RespondWithArticle().service(article, response);

		ArticleResponse dto = response.getObject().getBody();
		assertEquals(7L, dto.getId());
		assertEquals("Title", dto.getTitle());
	}

	// A producer: MockVar captures the published variable; a stubbed repository
	// stands in for the collaborator.
	@Test
	public void loadArticle_publishesTheEntity() throws Exception {
		Article stored = new Article(3L, "Stored", "body");
		ArticleRepository repository = mock(ArticleRepository.class);
		when(repository.findById(3L)).thenReturn(Optional.of(stored));

		MockVar<Article> loaded = new MockVar<>();
		new LoadArticle().service(3L, repository, loaded);

		assertSame(stored, loaded.get());
	}

	// The 404 branch is just a thrown exception. Assert it directly.
	@Test
	public void loadArticle_throwsWhenMissing() {
		ArticleRepository repository = mock(ArticleRepository.class);
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> new LoadArticle().service(99L, repository, new MockVar<>()));
	}

	// An action: verify it persisted the entity it was given.
	@Test
	public void saveArticle_persists() {
		Article article = new Article(5L, "T", "B");
		ArticleRepository repository = mock(ArticleRepository.class);

		new SaveArticle().service(article, repository);

		verify(repository).save(article);
	}
}
// END SNIPPET: tutorial
