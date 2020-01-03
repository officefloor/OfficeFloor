package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * 
 * Annotates a method with the name of the next method
 * ({@link ManagedFunction}).
 * 
 * @author Daniel Sagenschneider
 * @deprecated Use {@link Next}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated
public @interface NextFunction {

	/**
	 * Obtains the name of the next method.
	 * 
	 * @return Name of the next method.
	 */
	String value();

}