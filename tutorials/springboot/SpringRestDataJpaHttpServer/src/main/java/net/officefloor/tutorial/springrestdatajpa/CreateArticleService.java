package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

// START SNIPPET: tutorial
public class CreateArticleService {

	public void service(@RequestBody ArticleRequest request,
			ArticleRepository repository,
			ObjectResponse<ResponseEntity<ArticleResponse>> response) {
		Article saved = repository.save(new Article(null, request.getTitle(), request.getContent()));
		ArticleResponse dto = new ArticleResponse(saved.getId(), saved.getTitle(), saved.getContent());
		response.send(ResponseEntity
				.created(URI.create("/article/" + saved.getId()))
				.body(dto));
	}
}
// END SNIPPET: tutorial
