package net.officefloor.tutorial.springrestvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// START SNIPPET: tutorial
public class EvenQuantityValidator implements ConstraintValidator<EvenQuantity, Integer> {

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		if (value == null) {
			return true; // let @NotNull handle nulls
		}
		if (value % 2 == 0) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(
				"Quantity " + value + " is odd — items are sold in pairs")
				.addConstraintViolation();
		return false;
	}
}
// END SNIPPET: tutorial
