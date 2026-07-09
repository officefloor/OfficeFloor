package net.officefloor.tutorial.springrestrelationship;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

// START SNIPPET: tutorial
public class RespondWithAuthor {

	public void service(@Val Author author,
			ObjectResponse<ResponseEntity<AuthorResponse>> response) {
		response.send(ResponseEntity.ok(new AuthorResponse(author.getId(), author.getName())));
	}
}
// END SNIPPET: tutorial
