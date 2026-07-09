package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

// START SNIPPET: tutorial
/**
 * Shared responder. Both GET /article/{articleId} and
 * GET /author/{authorId}/article/{articleId} reuse it, because both end with an
 * Article to render.
 */
public class RespondWithArticle {

	public void service(@Val Article article,
			ObjectResponse<ResponseEntity<ArticleResponse>> response) {
		response.send(ResponseEntity.ok(toDto(article)));
	}

	static ArticleResponse toDto(Article article) {
		Long authorId = (article.getAuthor() != null) ? article.getAuthor().getId() : null;
		return new ArticleResponse(article.getId(), article.getTitle(), article.getContent(), authorId);
	}
}
// END SNIPPET: tutorial
