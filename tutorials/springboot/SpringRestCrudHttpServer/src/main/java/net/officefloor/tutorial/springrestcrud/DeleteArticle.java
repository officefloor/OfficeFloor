package net.officefloor.tutorial.springrestcrud;

import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
public class DeleteArticle {

	public void service(@Val Article article, ArticleRepository repository) {
		repository.delete(article);
	}
}
// END SNIPPET: tutorial
