package net.officefloor.plugin.managedobject.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Context for the {@link ClassFieldInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassInjectionInterrogatorContext {

	/**
	 * Obtains the {@link Class} of object for dependency injection.
	 * 
	 * @return {@link Class} of object for dependency injection.
	 */
	Class<?> getObjectClass();

	/**
	 * Registers a {@link Field} injection point.
	 * 
	 * @param field                 {@link Field} for injection.
	 * @param additionalAnnotations Additional {@link Annotation} instances relevant
	 *                              for the {@link Field} dependency injection.
	 */
	void registerInjectionPoint(Field field, Annotation... additionalAnnotations);

	/**
	 * Registers a {@link Method} injection point.
	 * 
	 * @param method                {@link Method} providing injection.
	 * @param additionalAnnotations Additional {@link Annotation} instances relevant
	 *                              for the {@link Field} dependency injection.
	 */
	void registerInjectionPoint(Method method, Annotation... additionalAnnotations);

}