package net.officefloor.spring;

/**
 * Qualfied bean for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class QualifiedBean {

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param value Value.
	 */
	public QualifiedBean(String value) {
		this.value = value;
	}

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	public String getValue() {
		return this.value;
	}

}