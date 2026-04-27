package net.officefloor.tutorial.springrestdatajpa;

import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class DeleteArticleService {

	public void service(@PathVariable(name = "id") Long id, ArticleRepository repository) {
		repository.deleteById(id);
	}
}
// END SNIPPET: tutorial
