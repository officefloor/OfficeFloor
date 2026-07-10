package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
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
