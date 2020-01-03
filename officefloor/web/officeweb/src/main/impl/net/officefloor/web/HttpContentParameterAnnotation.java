package net.officefloor.web;

/**
 * {@link HttpContentParameter} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpContentParameterAnnotation implements HttpParameterAnnotation {

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
	 * @param annotation {@link HttpContentParameter}.
	 */
	public HttpContentParameterAnnotation(HttpContentParameter annotation) {
		this.parameterName = annotation.value();
		this.qualifier = new HttpContentParameter.HttpContentParameterNameFactory().getQualifierName(annotation);
	}

	/**
	 * Instantiate.
	 * 
	 * @param parameterName Name to extract the parameter value.
	 */
	public HttpContentParameterAnnotation(String parameterName) {
		this.parameterName = parameterName;
		this.qualifier = HttpContentParameter.HttpContentParameterNameFactory.getQualifier(parameterName);
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