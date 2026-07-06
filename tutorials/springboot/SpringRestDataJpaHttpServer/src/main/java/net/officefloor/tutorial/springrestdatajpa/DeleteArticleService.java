package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class DeleteArticleService {

	public void service(@PathVariable(name = "id") Long id,
			ArticleRepository repository) throws ArticleNotFoundException {
		if (!repository.existsById(id)) {
			throw new ArticleNotFoundException(id);
		}
		repository.deleteById(id);
	}

	public void notFound(@Parameter ArticleNotFoundException ex,
			@HttpResponse(status = 404) ObjectResponse<String> response) {
		response.send("Article " + ex.getId() + " not found");
	}
}
// END SNIPPET: tutorial
