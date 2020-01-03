package net.officefloor.compile.impl.util;

/**
 * <p>
 * Extracts a {@link String} value from the input object.
 * <p>
 * Typically the extracted {@link String} will be used as a comparable key in
 * sorting a list of objects.
 * 
 * @author Daniel Sagenschneider
 */
public interface StringExtractor<T> {

	/**
	 * Extracts the {@link String} from the {@link Object}.
	 * 
	 * @param object
	 *            {@link Object} to extract the {@link String} from.
	 * @return Extracted {@link String}.
	 */
	String toString(T object);
}