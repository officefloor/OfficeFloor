package net.officefloor.tutorial.scratchopenapi;

import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
public class ApplyArticle {

	public void service(@Val Article article, @Val ArticleRequest request) {
		article.setTitle(request.getTitle());
		article.setContent(request.getContent());
	}
}
// END SNIPPET: tutorial
