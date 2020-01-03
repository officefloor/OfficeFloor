package net.officefloor.plugin.xml.unmarshall.load;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Loader to load objects onto the target object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectLoader {

	/**
	 * Method to load object onto the target object.
	 */
	protected final Method loadMethod;

	/**
	 * Type of object to be loaded to the target object.
	 */
	protected final Class<?> loadObjectType;

	/**
	 * Initiate.
	 * 
	 * @param loadMethod     Method to load object onto the target object.
	 * @param loadObjectType Type of object to be loaded to the target object.
	 */
	public ObjectLoader(Method loadMethod, Class<?> loadObjectType) {
		// Store state
		this.loadMethod = loadMethod;
		this.loadObjectType = loadObjectType;
	}

	/**
	 * Loads the object onto the target object and returns the loaded object.
	 * 
	 * @param targetObject Target object to have object loaded onto it.
	 * @return Object created and loaded onto the target object.
	 * @throws XmlMarshallException If fails to marshal the data onto the
	 *                              {@link Object}.
	 */
	public Object loadObject(Object targetObject) throws XmlMarshallException {

		// Create object to load
		Object loadObject;
		try {
			loadObject = this.loadObjectType.getDeclaredConstructor().newInstance();
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException("Illegal access to construction of '" + this.loadObjectType.getName() + "'",
					ex);
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException ex) {
			// Propagate failure
			throw new XmlMarshallException("Instantiation failure of '" + this.loadObjectType.getName() + "'",
					ex.getCause());
		}

		// Load the object
		try {
			// Load value onto target object
			this.loadMethod.invoke(targetObject, new Object[] { loadObject });
		} catch (IllegalArgumentException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invalid parameter to method '" + this.loadMethod.getName()
					+ "' which was of type '" + this.loadObjectType.getName() + "'", ex);
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException("Illegal access to method '" + this.loadMethod.getName() + "'", ex);
		} catch (InvocationTargetException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invoked load method failed.", ex.getCause());
		}

		// Return load object
		return loadObject;
	}

}
