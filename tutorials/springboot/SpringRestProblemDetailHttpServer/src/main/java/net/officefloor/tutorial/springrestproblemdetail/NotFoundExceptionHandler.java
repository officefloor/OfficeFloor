package net.officefloor.tutorial.springrestproblemdetail;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Instant;

// START SNIPPET: tutorial
public class NotFoundExceptionHandler {

	public void handle(@Parameter NotFoundException ex,
			ObjectResponse<ResponseEntity<ProblemDetail>> response) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		detail.setType(URI.create("https://example.com/problems/not-found"));
		detail.setTitle("Article not found");
		detail.setDetail("No article exists with id " + ex.getId());
		detail.setProperty("timestamp", Instant.now());
		response.send(ResponseEntity.status(404).body(detail));
	}
}
// END SNIPPET: tutorial
