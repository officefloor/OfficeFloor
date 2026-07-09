package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

import java.net.URI;

// START SNIPPET: tutorial
public class RespondWithArticleCreated {

	public void service(@Val Article article,
			ObjectResponse<ResponseEntity<ArticleResponse>> response) {
		ArticleResponse dto = RespondWithArticle.toDto(article);
		response.send(ResponseEntity
				.created(URI.create("/article/" + dto.getId()))
				.body(dto));
	}
}
// END SNIPPET: tutorial
