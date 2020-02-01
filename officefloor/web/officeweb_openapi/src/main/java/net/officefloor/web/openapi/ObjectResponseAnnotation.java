package net.officefloor.web.openapi;

import net.officefloor.web.ObjectResponse;

/**
 * Annotation providing details of {@link ObjectResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectResponseAnnotation {

	/**
	 * Response type.
	 */
	private final Class<?> responseType;

	/**
	 * Instantiate.
	 * 
	 * @param responseType Response type.
	 */
	public ObjectResponseAnnotation(Class<?> responseType) {
		this.responseType = responseType;
	}

	/**
	 * Obtains the response type.
	 * 
	 * @return Response type.
	 */
	public Class<?> getResponseType() {
		return this.responseType;
	}

}