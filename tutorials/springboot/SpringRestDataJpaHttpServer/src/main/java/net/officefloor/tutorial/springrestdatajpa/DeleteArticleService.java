package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class DeleteArticleService {

	public void service(@PathVariable(name = "id") Long id,
			ArticleRepository repository,
			@HttpResponse(status = 204) ObjectResponse<Void> response) {
		repository.deleteById(id);
		response.send(null);
	}
}
// END SNIPPET: tutorial
