package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.internal.structure.Flow;

/**
 * Annotates a method with the name of the next in the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Next {

	/**
	 * Obtains the name of the next in the {@link Flow}.
	 * 
	 * @return Name of the next in the {@link Flow}.
	 */
	String value();

}