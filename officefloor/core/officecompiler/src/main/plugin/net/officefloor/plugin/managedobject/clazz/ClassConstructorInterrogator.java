package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Constructor;

/**
 * Interrogates the {@link Class} for a {@link Constructor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassConstructorInterrogator {

	/**
	 * Interrogates for the {@link Constructor}.
	 * 
	 * @param context {@link ClassConstructorInterrogatorContext}.
	 * @return {@link Constructor}. Should be <code>null</code> to allow another
	 *         {@link ClassConstructorInterrogator} to find the {@link Constructor}.
	 * @throws Exception If fails to interrogate.
	 */
	Constructor<?> interrogate(ClassConstructorInterrogatorContext context) throws Exception;

}