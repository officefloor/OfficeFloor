package net.officefloor.tutorial.springrestvalidation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// START SNIPPET: tutorial
@Documented
@Constraint(validatedBy = EvenQuantityValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EvenQuantity {
	String message() default "Quantity must be even — items are sold in pairs";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
// END SNIPPET: tutorial
