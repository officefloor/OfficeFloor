package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Val;

import java.util.List;

// START SNIPPET: tutorial
public class ResolveTags {

	public void service(@Val Article article, @Val ArticleRequest request,
			TagRepository tagRepository) {
		List<Tag> managed = tagRepository.findByNameIn(request.getTags());
		article.getTags().clear();
		article.getTags().addAll(managed);
	}
}
// END SNIPPET: tutorial
