package net.officefloor.servlet.inject;

/**
 * Required dependency.
 * 
 * @author Daniel Sagenschneider
 */
public class RequiredDependency {

	/**
	 * Qualifier.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final Class<?> type;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 */
	public RequiredDependency(String qualifier, Class<?> type) {
		this.qualifier = qualifier;
		this.type = type;
	}

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier. May be <code>null</code>.
	 */
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * Obtains the type.
	 * 
	 * @return Type.
	 */
	public Class<?> getType() {
		return type;
	}

}