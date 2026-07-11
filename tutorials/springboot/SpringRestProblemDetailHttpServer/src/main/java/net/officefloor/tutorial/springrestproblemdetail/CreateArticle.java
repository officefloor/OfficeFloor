package net.officefloor.tutorial.springrestproblemdetail;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
@Validated
public class CreateArticle {

	public void service(@Valid @RequestBody ArticleRequest request,
			ArticleRepository repository,
			ObjectResponse<ResponseEntity<ArticleResponse>> response) {
		Article saved = repository.save(new Article(null, request.getTitle(), request.getContent()));
		response.send(ResponseEntity.status(201)
				.body(new ArticleResponse(saved.getId(), saved.getTitle(), saved.getContent())));
	}
}
// END SNIPPET: tutorial
