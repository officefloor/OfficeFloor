package net.officefloor.web;

/**
 * {@link HttpObject} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpObjectAnnotation {

	/**
	 * Accepted content types.
	 */
	private String[] acceptedContentTypes;

	/**
	 * Instantiate.
	 * 
	 * @param annotation {@link HttpObject}
	 */
	public HttpObjectAnnotation(HttpObject annotation) {
		this.acceptedContentTypes = annotation.acceptedContentTypes();
	}

	/**
	 * Instantiate.
	 * 
	 * @param acceptedContentTypes Accepted content types.
	 */
	public HttpObjectAnnotation(String... acceptedContentTypes) {
		this.acceptedContentTypes = acceptedContentTypes;
	}

	/**
	 * Obtains the accepted content types.
	 * 
	 * @return Accepted content types.
	 */
	public String[] getAcceptedContentTypes() {
		return this.acceptedContentTypes;
	}

}
