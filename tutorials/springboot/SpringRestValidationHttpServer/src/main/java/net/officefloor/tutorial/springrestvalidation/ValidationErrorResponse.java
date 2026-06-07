package net.officefloor.tutorial.springrestvalidation;

import java.util.List;
import java.util.Map;

// START SNIPPET: tutorial
public class ValidationErrorResponse {

	private final Map<String, List<String>> errors;

	public ValidationErrorResponse(Map<String, List<String>> errors) {
		this.errors = errors;
	}

	public Map<String, List<String>> getErrors() {
		return errors;
	}
}
// END SNIPPET: tutorial
