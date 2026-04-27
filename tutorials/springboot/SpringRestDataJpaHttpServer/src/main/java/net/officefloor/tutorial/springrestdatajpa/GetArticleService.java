package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class GetArticleService {

	public void service(@PathVariable(name = "id") Long id,
			ArticleRepository repository,
			ObjectResponse<ArticleResponse> response) {
		Article article = repository.findById(id).orElseThrow();
		response.send(new ArticleResponse(article.getId(), article.getTitle(), article.getContent()));
	}
}
// END SNIPPET: tutorial
