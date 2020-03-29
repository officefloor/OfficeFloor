package net.officefloor.compile;

/**
 * Context for the {@link OfficeFloorCompilerConfigurer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCompilerConfigurerContext {

	/**
	 * Obtains the {@link OfficeFloorCompiler} being configured.
	 * 
	 * @return {@link OfficeFloorCompiler} being configured.
	 */
	OfficeFloorCompiler getOfficeFloorCompiler();

	/**
	 * <p>
	 * Allows specifying another {@link ClassLoader}.
	 * <p>
	 * To ensure {@link Class} compatibility, the input {@link ClassLoader} must be
	 * a child of the current {@link OfficeFloorCompiler} {@link ClassLoader}.
	 * 
	 * @param classLoader {@link ClassLoader} that is child of
	 *                    {@link OfficeFloorCompiler} {@link ClassLoader}.
	 * @throws IllegalArgumentException If not a child.
	 */
	void setClassLoader(ClassLoader classLoader) throws IllegalArgumentException;

}