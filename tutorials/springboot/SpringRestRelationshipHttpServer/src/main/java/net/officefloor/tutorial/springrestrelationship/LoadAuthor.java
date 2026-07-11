package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Out;
import org.springframework.web.bind.annotation.PathVariable;

// START SNIPPET: tutorial
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
