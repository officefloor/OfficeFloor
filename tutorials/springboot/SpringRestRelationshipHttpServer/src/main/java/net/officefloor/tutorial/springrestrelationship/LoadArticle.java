package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
/**
 * Global load. Loads any article by id, regardless of author. Used by
 * GET /article/{articleId}. Contrast with {@link LoadAuthorsArticle}, which
 * only finds an article that belongs to a given author.
 */
public class LoadArticle {

	public void service(@PathVariable(name = "articleId") Long articleId,
			ArticleRepository repository,
			Out<Article> loaded) throws NotFoundException {
		Article article = repository.findById(articleId)
				.orElseThrow(() -> new NotFoundException(articleId));
		loaded.set(article);
	}
}
// END SNIPPET: tutorial
