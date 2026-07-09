package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;

// START SNIPPET: tutorial
/**
 * Builds a new article and attaches it to the author loaded by
 * {@link LoadAuthor}. It consumes two variables: the parent author and the
 * validated request body. The author comes from the URL, not the body.
 */
public class BuildAuthorsArticle {

	public void service(@Val Author author, @Val ArticleRequest request, Out<Article> built) {
		Article article = new Article(null, request.getTitle(), request.getContent());
		article.setAuthor(author);
		built.set(article);
	}
}
// END SNIPPET: tutorial
