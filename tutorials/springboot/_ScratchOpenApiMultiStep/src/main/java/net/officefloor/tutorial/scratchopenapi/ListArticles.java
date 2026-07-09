package net.officefloor.tutorial.scratchopenapi;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

// START SNIPPET: tutorial
public class ListArticles {

	public void service(ArticleRepository repository,
			ObjectResponse<ResponseEntity<List<ArticleResponse>>> response) {
		List<ArticleResponse> articles = repository.findAll().stream()
				.map(article -> new ArticleResponse(article.getId(), article.getTitle(), article.getContent()))
				.toList();
		response.send(ResponseEntity.ok(articles));
	}
}
// END SNIPPET: tutorial
