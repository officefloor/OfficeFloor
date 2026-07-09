package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
public class SaveArticle {

	public void service(@Val Article article, ArticleRepository repository) {
		repository.save(article);
	}
}
// END SNIPPET: tutorial
