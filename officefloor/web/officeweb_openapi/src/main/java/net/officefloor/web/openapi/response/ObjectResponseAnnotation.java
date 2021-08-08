/*-
 * #%L
 * OpenAPI
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

package net.officefloor.web.openapi.response;

import java.lang.reflect.Type;

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
	private final Type responseType;

	/**
	 * Instantiate.
	 * 
	 * @param statusCode   Status code for {@link HttpResponse}.
	 * @param responseType Response type.
	 */
	public ObjectResponseAnnotation(int statusCode, Type responseType) {
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
	public Type getResponseType() {
		return this.responseType;
	}

}
