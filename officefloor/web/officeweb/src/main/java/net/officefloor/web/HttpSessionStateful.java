package net.officefloor.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.object.HttpSessionObjectManagedObjectSource;

/**
 * <p>
 * Annotated on the class of the parameter to indicate it should be a
 * {@link HttpSessionObjectManagedObjectSource}.
 * <p>
 * This simplifies means to specifying
 * {@link HttpSessionObjectManagedObjectSource} instances by in-lining it with
 * the code.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpSessionStateful {

	/**
	 * Allows specifying the name to bind the object into the
	 * {@link HttpSession}.
	 * 
	 * @return Name to bind the object into the {@link HttpSession}. The blank
	 *         default value indicates for the {@link ManagedObject} to assign
	 *         its own unique value.
	 */
	String bind() default "";

}