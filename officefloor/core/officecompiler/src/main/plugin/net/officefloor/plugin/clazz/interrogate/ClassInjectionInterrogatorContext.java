package net.officefloor.plugin.clazz.interrogate;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Context for the {@link ClassInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassInjectionInterrogatorContext {

	/**
	 * <p>
	 * Obtains the {@link AnnotatedElement}.
	 * <p>
	 * Typically this is either a {@link Field} or {@link Method}.
	 * 
	 * @return {@link AnnotatedElement}.
	 */
	AnnotatedElement getAnnotatedElement();

	/**
	 * Obtains the {@link Class} of object for dependency injection.
	 * 
	 * @return {@link Class} of object for dependency injection.
	 */
	Class<?> getObjectClass();

	/**
	 * <p>
	 * Registers a injection point.
	 * <p>
	 * Should always be valid if passing in the return of
	 * {@link #getAnnotatedElement()}.
	 * 
	 * @param member {@link Field} or {@link Method} for injection.
	 * @throws IllegalArgumentException If invalid injection point type.
	 */
	void registerInjectionPoint(AnnotatedElement member) throws IllegalArgumentException;

	/**
	 * Registers a post construct {@link Method}.
	 * 
	 * @param method {@link Method} to be invoked after all dependencies are
	 *               injected.
	 */
	void registerPostConstruct(Method method);

}