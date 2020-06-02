package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Method;

/**
 * Interrogator of the {@link Class} {@link Method} to determine if requires
 * injection.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassMethodInjectionInterrogator {

	/**
	 * Interrogates for injection points.
	 * 
	 * @param context {@link ClassMethodInjectionInterrogatorContext}.
	 * @throws Exception If failure in interrogation.
	 */
	void interrogate(ClassMethodInjectionInterrogatorContext context) throws Exception;

}