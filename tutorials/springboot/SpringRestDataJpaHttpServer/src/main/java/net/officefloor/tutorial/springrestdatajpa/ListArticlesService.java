package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.web.ObjectResponse;

import java.util.List;
import java.util.stream.Collectors;

// START SNIPPET: tutorial
public class ListArticlesService {

	public void service(ArticleRepository repository, ObjectResponse<List<ArticleResponse>> response) {
		List<ArticleResponse> articles = repository.findAll().stream()
				.map(a -> new ArticleResponse(a.getId(), a.getTitle(), a.getContent()))
				.collect(Collectors.toList());
		response.send(articles);
	}
}
// END SNIPPET: tutorial
