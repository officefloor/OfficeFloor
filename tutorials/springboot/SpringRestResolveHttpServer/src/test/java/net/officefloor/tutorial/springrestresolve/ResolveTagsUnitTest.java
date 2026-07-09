package net.officefloor.tutorial.springrestresolve;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// START SNIPPET: tutorial
/**
 * ResolveTags takes the entity, the request and a repository, no Out. So it
 * unit tests with plain objects and a stubbed repository. No Spring, no server.
 */
public class ResolveTagsUnitTest {

	@Test
	public void setsTheManagedTagsNamedInTheRequest() {
		Article article = new Article(1L, "Title");
		ArticleRequest request = new ArticleRequest("Title", List.of("java", "spring"));

		// Repository returns the managed tags (with ids) for those names
		TagRepository repository = mock(TagRepository.class);
		when(repository.findByNameIn(anyCollection()))
				.thenReturn(List.of(new Tag(10L, "java"), new Tag(20L, "spring")));

		new ResolveTags().service(article, request, repository);

		// The article now holds the managed tags, with ids
		Set<Long> ids = article.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
		assertEquals(Set.of(10L, 20L), ids);
	}
}
// END SNIPPET: tutorial
