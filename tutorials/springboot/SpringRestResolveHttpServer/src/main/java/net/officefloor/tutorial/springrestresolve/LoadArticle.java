package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Out;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
public class LoadArticle {

	public void service(@PathVariable(name = "id") Long id,
			ArticleRepository repository,
			Out<Article> loaded) throws NotFoundException {
		Article article = repository.findById(id)
				.orElseThrow(() -> new NotFoundException(id));
		loaded.set(article);
	}
}
// END SNIPPET: tutorial
