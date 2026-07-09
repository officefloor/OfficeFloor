package net.officefloor.tutorial.springrestresolve;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

// START SNIPPET: tutorial
public class RespondWithArticle {

	public void service(@Val Article article,
			ObjectResponse<ResponseEntity<ArticleResponse>> response) {
		response.send(ResponseEntity.ok(toDto(article)));
	}

	static ArticleResponse toDto(Article article) {
		List<String> tags = article.getTags().stream()
				.map(Tag::getName)
				.collect(Collectors.toList());
		return new ArticleResponse(article.getId(), article.getTitle(), tags);
	}
}
// END SNIPPET: tutorial
