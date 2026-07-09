package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
/**
 * Ownership-scoped load. Consumes the author loaded by {@link LoadAuthor} and
 * finds the article <em>within that author</em>. An article that exists but
 * belongs to a different author is not found, so the endpoint returns 404.
 *
 * <p>Do NOT replace this with the global {@link LoadArticle}. The global load
 * would return another author's article under
 * {@code /author/{authorId}/article/{articleId}}, which leaks data across
 * authors. The scoping is the contract of the nested URL, so the scoped load is
 * a distinct function.
 */
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
