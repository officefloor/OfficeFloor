package net.officefloor.plugin.clazz.interrogate;

/**
 * Interrogator of the {@link Class} members to determine if requires injection.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassInjectionInterrogator {

	/**
	 * Interrogates for injection points.
	 * 
	 * @param context {@link ClassInjectionInterrogatorContext}.
	 * @throws Exception If failure in interrogation.
	 */
	void interrogate(ClassInjectionInterrogatorContext context) throws Exception;

}