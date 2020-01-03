package net.officefloor.spring;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Spring dependency on {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringDependency {

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * Instantiate.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type.
	 */
	public SpringDependency(String qualifier, Class<?> objectType) {
		this.qualifier = qualifier;
		this.objectType = objectType;
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
	 * Obtains the object type.
	 * 
	 * @return Object type.
	 */
	public Class<?> getObjectType() {
		return objectType;
	}

}