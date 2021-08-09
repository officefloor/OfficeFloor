/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
