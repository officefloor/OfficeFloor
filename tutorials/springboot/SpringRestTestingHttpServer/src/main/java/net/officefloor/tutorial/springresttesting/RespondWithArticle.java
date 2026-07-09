package net.officefloor.tutorial.springresttesting;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

// START SNIPPET: tutorial
public class RespondWithArticle {

	public void service(@Val Article article,
			ObjectResponse<ResponseEntity<ArticleResponse>> response) {
		ArticleResponse dto = new ArticleResponse(article.getId(), article.getTitle(), article.getContent());
		response.send(ResponseEntity.ok(dto));
	}
}
// END SNIPPET: tutorial
