package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
/**
 * Shared load. This one function loads an author by id and publishes it. Every
 * author-scoped endpoint (get the author, get the author's article, create an
 * article for the author) reuses it. There is one place that turns an authorId
 * into an Author, and one place that decides a missing author is a 404.
 */
public class LoadAuthor {

	public void service(@PathVariable(name = "authorId") Long authorId,
			AuthorRepository repository,
			Out<Author> loaded) throws NotFoundException {
		Author author = repository.findById(authorId)
				.orElseThrow(() -> new NotFoundException(authorId));
		loaded.set(author);
	}
}
// END SNIPPET: tutorial
