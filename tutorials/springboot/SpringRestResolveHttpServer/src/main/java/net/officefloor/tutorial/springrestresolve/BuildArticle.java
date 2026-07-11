package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
public class BuildArticle {

	public void service(@Val ArticleRequest request, Out<Article> built) {
		built.set(new Article(null, request.getTitle()));
	}
}
// END SNIPPET: tutorial
