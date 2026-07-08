package net.officefloor.tutorial.springrestcrud;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
@Validated
public class ValidateArticle {

	public void service(@Valid @RequestBody ArticleRequest request,
			Out<ArticleRequest> validated) {
		validated.set(request);
	}
}
// END SNIPPET: tutorial
