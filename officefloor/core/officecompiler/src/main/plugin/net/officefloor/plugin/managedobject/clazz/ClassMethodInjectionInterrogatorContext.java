package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Method;

/**
 * Context for the {@link ClassMethodInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassMethodInjectionInterrogatorContext extends ClassInjectionInterrogatorContext {

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	Method getMethod();

}