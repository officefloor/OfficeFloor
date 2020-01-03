package net.officefloor.web;

/**
 * {@link HttpCookieParameter} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCookieParameterAnnotation implements HttpParameterAnnotation {

	/**
	 * Parameter name.
	 */
	private final String parameterName;

	/**
	 * Qualifier.
	 */
	private final String qualifier;

	/**
	 * Instantiate.
	 * 
	 * @param annotation {@link HttpCookieParameter}.
	 */
	public HttpCookieParameterAnnotation(HttpCookieParameter annotation) {
		this.parameterName = annotation.value();
		this.qualifier = new HttpCookieParameter.HttpCookieParameterNameFactory().getQualifierName(annotation);
	}

	/**
	 * Instantiate.
	 * 
	 * @param parameterName Name to extract the parameter value.
	 */
	public HttpCookieParameterAnnotation(String parameterName) {
		this.parameterName = parameterName;
		this.qualifier = HttpCookieParameter.HttpCookieParameterNameFactory.getQualifier(parameterName);
	}

	/*
	 * =================== HttpParameterAnnotation ===========================
	 */

	@Override
	public String getParameterName() {
		return this.parameterName;
	}

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

}