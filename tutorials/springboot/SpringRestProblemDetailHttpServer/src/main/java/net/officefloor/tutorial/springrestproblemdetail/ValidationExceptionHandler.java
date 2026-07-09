package net.officefloor.tutorial.springrestproblemdetail;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// START SNIPPET: tutorial
/**
 * Maps a framework {@link MethodArgumentNotValidException} to a 400 Problem
 * Detail, adding a field-by-field breakdown so clients can show which fields
 * failed and why.
 */
public class ValidationExceptionHandler {

	public void handle(@Parameter MethodArgumentNotValidException ex,
			ObjectResponse<ResponseEntity<ProblemDetail>> response) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}

		ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		detail.setType(URI.create("https://example.com/problems/validation"));
		detail.setTitle("Request validation failed");
		detail.setDetail("The request has " + errors.size() + " invalid field(s)");
		detail.setProperty("timestamp", Instant.now());
		detail.setProperty("errors", errors);
		response.send(ResponseEntity.status(400).body(detail));
	}
}
// END SNIPPET: tutorial
