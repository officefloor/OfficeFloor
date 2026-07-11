package net.officefloor.tutorial.springrestfilter;

import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;

// START SNIPPET: tutorial
public class ListArticlesPage {

	public void service(
			@RequestParam(name = "category", required = false) String category,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			ArticleRepository repository,
			ObjectResponse<ArticlePage> response) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
		Page<Article> result = (category != null)
				? repository.findByCategory(category, pageable)
				: repository.findAll(pageable);
		response.send(new ArticlePage(
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages(),
				result.getContent().stream().map(ListArticles::toDto).collect(Collectors.toList())));
	}
}
// END SNIPPET: tutorial
