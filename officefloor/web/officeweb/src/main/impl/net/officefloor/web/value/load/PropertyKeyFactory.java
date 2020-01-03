package net.officefloor.web.value.load;

/**
 * Factory for a {@link PropertyKey}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyKeyFactory {

	/**
	 * Indicates if case insensitive match.
	 */
	private final boolean isCaseInsensitive;

	/**
	 * Initiate.
	 * 
	 * @param isCaseInsensitive
	 *            Indicates if case insensitive match.
	 */
	public PropertyKeyFactory(boolean isCaseInsensitive) {
		this.isCaseInsensitive = isCaseInsensitive;
	}

	/**
	 * Creates the {@link PropertyKey}.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @return {@link PropertyKey}.
	 */
	public PropertyKey createPropertyKey(String propertyName) {
		return new PropertyKey(propertyName, this.isCaseInsensitive);
	}

}