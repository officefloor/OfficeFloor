/*-
 * #%L
 * OpenAPI
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
