package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Field;

/**
 * Context for the {@link ClassFieldInjectionInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassFieldInjectionInterrogatorContext extends ClassInjectionInterrogatorContext {

	/**
	 * Obtains the {@link Field}.
	 * 
	 * @return {@link Field}.
	 */
	Field getField();

}