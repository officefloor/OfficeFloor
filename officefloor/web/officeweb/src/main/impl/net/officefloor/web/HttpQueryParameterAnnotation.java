package net.officefloor.web;

/**
 * {@link HttpQueryParameter} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpQueryParameterAnnotation implements HttpParameterAnnotation {

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
	 * @param annotation {@link HttpQueryParameter}.
	 */
	public HttpQueryParameterAnnotation(HttpQueryParameter annotation) {
		this.parameterName = annotation.value();
		this.qualifier = new HttpQueryParameter.HttpQueryParameterNameFactory().getQualifierName(annotation);
	}

	/**
	 * Instantiate.
	 * 
	 * @param parameterName Name to extract the parameter value.
	 */
	public HttpQueryParameterAnnotation(String parameterName) {
		this.parameterName = parameterName;
		this.qualifier = HttpQueryParameter.HttpQueryParameterNameFactory.getQualifier(parameterName);
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