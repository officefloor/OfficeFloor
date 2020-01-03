package net.officefloor.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestState;

/**
 * <p>
 * Annotated on the class of the parameter to indicate it should be a
 * {@link HttpRequestObjectManagedObjectSource}.
 * <p>
 * This simplifies means to specifying
 * {@link HttpRequestObjectManagedObjectSource} instances by in-lining it with
 * the code.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpRequestStateful {

	/**
	 * Allows specifying the name to bind the object into the
	 * {@link HttpRequestState}.
	 * 
	 * @return Name to bind the object into the {@link HttpRequestState}. The
	 *         blank default value indicates for the {@link ManagedObject} to
	 *         assign its own unique value.
	 */
	String value() default "";

}