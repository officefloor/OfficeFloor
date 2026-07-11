package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

// START SNIPPET: tutorial
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
