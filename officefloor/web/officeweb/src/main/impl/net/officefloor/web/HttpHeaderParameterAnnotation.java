package net.officefloor.web;

/**
 * {@link HttpHeaderParameter} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderParameterAnnotation implements HttpParameterAnnotation {

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
	 * @param annotation {@link HttpHeaderParameter}.
	 */
	public HttpHeaderParameterAnnotation(HttpHeaderParameter annotation) {
		this.parameterName = annotation.value();
		this.qualifier = new HttpHeaderParameter.HttpHeaderParameterNameFactory().getQualifierName(annotation);
	}

	/**
	 * Instantiate.
	 * 
	 * @param parameterName Name to extract the parameter value.
	 */
	public HttpHeaderParameterAnnotation(String parameterName) {
		this.parameterName = parameterName;
		this.qualifier = HttpHeaderParameter.HttpHeaderParameterNameFactory.getQualifier(parameterName);
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