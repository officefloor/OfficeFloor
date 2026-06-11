package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;

// START SNIPPET: tutorial
public class ListArticlesPageService {

	public void service(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			ArticleRepository repository,
			ObjectResponse<ArticlePage> response) {
		Page<Article> result = repository.findAll(
				PageRequest.of(page, size, Sort.by("id")));
		response.send(new ArticlePage(
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages(),
				result.getContent().stream()
						.map(a -> new ArticleResponse(a.getId(), a.getTitle(), a.getContent()))
						.collect(Collectors.toList())));
	}
}
// END SNIPPET: tutorial
