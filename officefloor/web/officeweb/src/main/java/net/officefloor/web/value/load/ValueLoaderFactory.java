package net.officefloor.web.value.load;

/**
 * Factory for the creation of a {@link ValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueLoaderFactory<T> {

	/**
	 * Creates the {@link ValueLoader} for the object.
	 * 
	 * @param object
	 *            Object to have values loaded onto it.
	 * @return {@link ValueLoader} for the object.
	 * @throws Exception
	 *             If fails to create the {@link ValueLoader}.
	 */
	ValueLoader createValueLoader(T object) throws Exception;

}