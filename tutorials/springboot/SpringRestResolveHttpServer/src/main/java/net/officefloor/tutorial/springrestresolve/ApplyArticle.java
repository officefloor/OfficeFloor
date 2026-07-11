package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
public class ApplyArticle {

	public void service(@Val Article article, @Val ArticleRequest request) {
		article.setTitle(request.getTitle());
	}
}
// END SNIPPET: tutorial
