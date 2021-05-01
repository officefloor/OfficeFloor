package net.officefloor.compile.impl.adapt;

import java.util.List;

/**
 * Extracts the values of a {@link List}.
 * 
 * @author Daniel Sagenschneider
 */
public class ListAdapter {

	/**
	 * Translates {@link List} to array of its values.
	 * 
	 * @param list {@link List}.
	 * @return Array of {@link List} values.
	 */
	public static Object[] toArray(Object list) {
		return ((List<?>) list).toArray();
	}

}