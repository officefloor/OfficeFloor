package net.officefloor.tutorial.springrestfilter;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

// START SNIPPET: tutorial
/**
 * Lists articles, with an optional category filter.
 *
 * <p>The {@code category} query parameter is {@code required = false}. When it
 * is absent the list is unfiltered. A collection read like this has no branching
 * pipeline, so it stays a single function.
 */
public class ListArticles {

	public void service(
			@RequestParam(name = "category", required = false) String category,
			ArticleRepository repository,
			ObjectResponse<List<ArticleResponse>> response) {
		List<Article> articles = (category != null)
				? repository.findByCategory(category)
				: repository.findAll();
		response.send(articles.stream().map(ListArticles::toDto).collect(Collectors.toList()));
	}

	static ArticleResponse toDto(Article article) {
		return new ArticleResponse(article.getId(), article.getTitle(), article.getCategory());
	}
}
// END SNIPPET: tutorial
