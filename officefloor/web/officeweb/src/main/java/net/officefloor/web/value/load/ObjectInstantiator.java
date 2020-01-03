package net.officefloor.web.value.load;

/**
 * Responsible for instantiating the object instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectInstantiator {

	/**
	 * Instantiates the object.
	 * 
	 * @param <T>
	 *            Object type.
	 * @param clazz
	 *            Class of object to instantiate.
	 * @return Instantiated object.
	 * @throws Exception
	 *             If fails to instantiate the object.
	 */
	<T> T instantiate(Class<T> clazz) throws Exception;

}