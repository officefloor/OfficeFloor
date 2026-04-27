package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
public class CreateArticleService {

	public void service(@RequestBody ArticleRequest request,
			ArticleRepository repository,
			@HttpResponse(status = 201) ObjectResponse<ArticleResponse> response) {
		Article saved = repository.save(new Article(null, request.getTitle(), request.getContent()));
		response.send(new ArticleResponse(saved.getId(), saved.getTitle(), saved.getContent()));
	}
}
// END SNIPPET: tutorial
