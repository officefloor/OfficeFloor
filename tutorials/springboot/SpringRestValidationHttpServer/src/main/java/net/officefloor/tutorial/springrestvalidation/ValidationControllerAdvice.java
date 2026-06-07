package net.officefloor.tutorial.springrestvalidation;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// START SNIPPET: tutorial
@RestControllerAdvice
public class ValidationControllerAdvice {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ValidationErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
		Map<String, List<String>> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.computeIfAbsent(fieldError.getField(), k -> new ArrayList<>())
					.add(fieldError.getDefaultMessage());
		}
		return new ValidationErrorResponse(errors);
	}
}
// END SNIPPET: tutorial
