package net.officefloor.tutorial.springrestproblemdetail;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Instant;

// START SNIPPET: tutorial
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
