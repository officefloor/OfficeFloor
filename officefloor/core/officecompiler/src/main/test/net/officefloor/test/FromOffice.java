package net.officefloor.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.manage.Office;

/**
 * Allows specifying to load test dependency from a particular {@link Office}
 * (rather than default {@link Office}).
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface FromOffice {

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	String value();
}