package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
/**
 * Builds a new article from the request. It sets the scalar fields only. The
 * tags are references to other entities, so they are handled by ResolveTags.
 */
public class BuildArticle {

	public void service(@Val ArticleRequest request, Out<Article> built) {
		built.set(new Article(null, request.getTitle()));
	}
}
// END SNIPPET: tutorial
