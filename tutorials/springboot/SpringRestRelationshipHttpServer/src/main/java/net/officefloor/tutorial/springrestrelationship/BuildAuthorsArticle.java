package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
public class BuildAuthorsArticle {

	public void service(@Val Author author, @Val ArticleRequest request, Out<Article> built) {
		Article article = new Article(null, request.getTitle(), request.getContent());
		article.setAuthor(author);
		built.set(article);
	}
}
// END SNIPPET: tutorial
