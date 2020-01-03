package net.officefloor.plugin.xml.unmarshall.load;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Loader to load a static value onto target object.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticValueLoader extends AbstractValueLoader {

	/**
	 * Static value to load onto target object.
	 */
	protected final Object value;

	/**
	 * Initiate with details to static load.
	 * 
	 * @param loadMethod
	 *            Method to load static value onto target object.
	 * @param value
	 *            Static value to load onto target object.
	 */
	public StaticValueLoader(Method loadMethod, Object value) {
		super(loadMethod);

		// Store state
		this.value = value;
	}

	/**
	 * Loads the static value onto the target object.
	 * 
	 * @param targetObject
	 *            Target object to receive the static value.
	 * @throws XmlMarshallException
	 *             If fails to load the static value to target object.
	 */
	public void loadValue(Object targetObject) throws XmlMarshallException {
		// Set value on target object
		this.setValue(targetObject, this.value);
	}

}
