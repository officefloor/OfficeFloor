package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Val;

import java.util.List;

// START SNIPPET: tutorial
/**
 * Resolve function. It enriches the article by looking up the managed Tag
 * entities named in the request and setting them on the article.
 *
 * <p>It is an action, like Apply, but it does a repository lookup rather than
 * copying a field. It runs after Build or Apply and before Save. It mutates the
 * article's tag collection in place, so the following Save persists the managed
 * references. The same function serves both the create and the update pipeline.
 *
 * <p>Mutating the collection in place (clear then add) rather than replacing it
 * keeps the managed entity's collection tracked by the persistence context.
 * The looked-up tags are already managed, so nothing transient is ever saved.
 */
public class ResolveTags {

	public void service(@Val Article article, @Val ArticleRequest request,
			TagRepository tagRepository) {
		List<Tag> managed = tagRepository.findByNameIn(request.getTags());
		article.getTags().clear();
		article.getTags().addAll(managed);
	}
}
// END SNIPPET: tutorial
