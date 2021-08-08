package net.officefloor.cabinet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifying {@link Key} for the {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface Key {
}