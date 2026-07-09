package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
/**
 * Applies the request to a loaded article. Like BuildArticle, it sets the
 * scalar fields only. ResolveTags handles the tag references.
 */
public class ApplyArticle {

	public void service(@Val Article article, @Val ArticleRequest request) {
		article.setTitle(request.getTitle());
	}
}
// END SNIPPET: tutorial
