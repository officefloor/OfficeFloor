package net.officefloor.tutorial.springrestcrud;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

import java.net.URI;

// START SNIPPET: tutorial
public class RespondWithArticleCreated {

	public void service(@Val Article article,
			ObjectResponse<ResponseEntity<ArticleResponse>> response) {
		ArticleResponse dto = new ArticleResponse(article.getId(), article.getTitle(), article.getContent());
		response.send(ResponseEntity
				.created(URI.create("/article/" + article.getId()))
				.body(dto));
	}
}
// END SNIPPET: tutorial
