package net.officefloor.tutorial.springrestfilter;

import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;

// START SNIPPET: tutorial
/**
 * Lists articles with the optional category filter and pagination together.
 *
 * <p>{@code category} is optional. {@code page} and {@code size} have defaults,
 * so a request needs neither. The filter and the paging combine into one
 * Spring Data query.
 */
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
