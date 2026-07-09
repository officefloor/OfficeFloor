package net.officefloor.tutorial.springrestproblemdetail;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Instant;

// START SNIPPET: tutorial
/**
 * Catch-all handler. Any exception with no more specific handler maps to a 500
 * Problem Detail. It uses a generic message so internal detail is never leaked
 * to the client.
 */
public class GeneralExceptionHandler {

	public void handle(@Parameter Exception ex,
			ObjectResponse<ResponseEntity<ProblemDetail>> response) {
		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		detail.setType(URI.create("about:blank"));
		detail.setTitle("Internal server error");
		detail.setDetail("An unexpected error occurred while processing the request");
		detail.setProperty("timestamp", Instant.now());
		response.send(ResponseEntity.status(500).body(detail));
	}
}
// END SNIPPET: tutorial
