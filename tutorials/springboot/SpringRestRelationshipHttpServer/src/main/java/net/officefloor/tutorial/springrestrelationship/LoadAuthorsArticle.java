package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class LoadAuthorsArticle {

	public void service(@Val Author author,
			@PathVariable(name = "articleId") Long articleId,
			Out<Article> loaded) throws NotFoundException {
		Article article = author.getArticle(articleId);
		if (article == null) {
			throw new NotFoundException(articleId);
		}
		loaded.set(article);
	}
}
// END SNIPPET: tutorial
