package net.officefloor.web;

/**
 * {@link HttpPathParameter} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpPathParameterAnnotation implements HttpParameterAnnotation {

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
	 * @param annotation {@link HttpPathParameter}.
	 */
	public HttpPathParameterAnnotation(HttpPathParameter annotation) {
		this.parameterName = annotation.value();
		this.qualifier = new HttpPathParameter.HttpPathParameterNameFactory().getQualifierName(annotation);
	}

	/**
	 * Instantiate.
	 * 
	 * @param parameterName Name to extract the parameter value.
	 */
	public HttpPathParameterAnnotation(String parameterName) {
		this.parameterName = parameterName;
		this.qualifier = HttpPathParameter.HttpPathParameterNameFactory.getQualifier(parameterName);
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