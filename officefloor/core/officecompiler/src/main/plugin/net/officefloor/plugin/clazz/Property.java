package net.officefloor.plugin.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flags to inject a {@link net.officefloor.compile.properties.Property} value.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Property {

	/**
	 * Name of the {@link net.officefloor.compile.properties.Property}.
	 * 
	 * @return Name.
	 */
	String value();

}