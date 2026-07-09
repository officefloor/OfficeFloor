package net.officefloor.tutorial.scratchopenapi;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

// START SNIPPET: tutorial
public class ValidationExceptionHandler {

	public void handle(@Parameter MethodArgumentNotValidException ex,
			ObjectResponse<ResponseEntity<String>> response) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + " " + error.getDefaultMessage())
				.findFirst().orElse("Invalid request");
		response.send(ResponseEntity.badRequest().body(message));
	}
}
// END SNIPPET: tutorial
