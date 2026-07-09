package net.officefloor.tutorial.springrestproblemdetail;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
/**
 * Reads an article, or throws {@link NotFoundException} when it is absent.
 * The endpoints here are kept as single functions so the focus stays on the
 * error responses. The escalation handlers turn the thrown exceptions into
 * Problem Detail responses.
 */
public class GetArticle {

	public void service(@PathVariable(name = "id") Long id,
			ArticleRepository repository,
			ObjectResponse<ArticleResponse> response) throws NotFoundException {
		Article article = repository.findById(id)
				.orElseThrow(() -> new NotFoundException(id));
		response.send(new ArticleResponse(article.getId(), article.getTitle(), article.getContent()));
	}
}
// END SNIPPET: tutorial
