package net.officefloor.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.web.state.HttpApplicationState;

/**
 * Annotated on the class of the parameter to indicate it should be bound to the
 * application state. This allows for in-line configuration of application
 * objects.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpApplicationStateful {

	/**
	 * Allows specifying the name to bind the object into the
	 * {@link HttpApplicationState}.
	 * 
	 * @return Name to bind the object into the {@link HttpApplicationState}.
	 *         The blank default value indicates for the {@link ManagedObject}
	 *         to assign its own unique value.
	 */
	String bind() default "";

}