package net.officefloor.gef.configurer;

import java.util.function.Function;

/**
 * {@link Function} interface to load the value.
 *
 * @author Daniel Sagenschneider
 */
public interface ValueLoader<M, V> {

	/**
	 * Loads the value to the model.
	 * 
	 * @param model
	 *            Model.
	 * @param value
	 *            Value.
	 */
	void loadValue(M model, V value);
}