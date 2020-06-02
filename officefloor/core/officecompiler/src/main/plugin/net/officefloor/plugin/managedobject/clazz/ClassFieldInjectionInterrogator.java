package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;

/**
 * Interrogator of the {@link Class} {@link Field} to determine if requires
 * injection.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassFieldInjectionInterrogator {

	/**
	 * Interrogates for injection points.
	 * 
	 * @param context {@link ClassFieldInjectionInterrogatorContext}.
	 * @throws Exception If failure in interrogation.
	 */
	void interrogate(ClassFieldInjectionInterrogatorContext context) throws Exception;

}