package net.officefloor.web.openapi.response;

import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;

/**
 * Annotation providing details of {@link ObjectResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectResponseAnnotation {

	/**
	 * Status code for {@link HttpResponse}.
	 */
	private final int statusCode;

	/**
	 * Response type.
	 */
	private final Class<?> responseType;

	/**
	 * Instantiate.
	 * 
	 * @param statusCode   Status code for {@link HttpResponse}.
	 * @param responseType Response type.
	 */
	public ObjectResponseAnnotation(int statusCode, Class<?> responseType) {
		this.statusCode = statusCode;
		this.responseType = responseType;
	}

	/**
	 * Obtains the status code.
	 * 
	 * @return Status code.
	 */
	public int getStatusCode() {
		return this.statusCode;
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