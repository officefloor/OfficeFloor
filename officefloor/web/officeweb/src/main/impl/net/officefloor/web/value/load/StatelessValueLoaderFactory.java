package net.officefloor.web.value.load;

/**
 * Factory to create the {@link StatelessValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface StatelessValueLoaderFactory {

	/**
	 * Obtains the property name for the {@link StatelessValueLoader}.
	 * 
	 * @return Property name for the {@link StatelessValueLoader}.
	 */
	String getPropertyName();

	/**
	 * Creates the {@link StatelessValueLoader}.
	 * 
	 * @param clazz
	 *            {@link StatelessValueLoader} will be specific to the {@link Class}.
	 * @return {@link StatelessValueLoader}.
	 * @throws Exception
	 *             If fails to create the {@link StatelessValueLoader}.
	 */
	StatelessValueLoader createValueLoader(Class<?> clazz) throws Exception;

}