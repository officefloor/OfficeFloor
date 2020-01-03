package net.officefloor.web;

/**
 * HTTP parameter annotation.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpParameterAnnotation {

	/**
	 * Obtains the name to extract the parameter value.
	 * 
	 * @return Name to extract the parameter value.
	 */
	String getParameterName();

	/**
	 * Obtains the qualifier.
	 * 
	 * @return Qualifier.
	 */
	String getQualifier();

}