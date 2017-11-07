/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.build;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.web.state.HttpCookie;

/**
 * Input details for the application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInputBuilder {

	/**
	 * Adds a parameter from the query.
	 * 
	 * @param name
	 *            Name of parameter.
	 */
	HttpParameterBuilder addQueryParameter(String name);

	/**
	 * Adds a parameter from a {@link HttpHeader}.
	 * 
	 * @param name
	 *            Name of parameter.
	 */
	HttpParameterBuilder addHeaderParameter(String name);

	/**
	 * Adds a parameter from a {@link HttpCookie}.
	 * 
	 * @param name
	 *            Name of parameter.
	 */
	HttpParameterBuilder addCookieParameter(String name);

	/**
	 * Adds an {@link HttpArgumentParser} for the {@link HttpInputBuilder}.
	 * 
	 * @param argumentParser
	 *            {@link HttpArgumentParser} for the {@link HttpInputBuilder}.
	 */
	HttpContentParametersBuilder addEntityParameters(HttpArgumentParser argumentParser);

	/**
	 * Adds an {@link HttpObjectParser} for the {@link HttpInputBuilder}.
	 * 
	 * @param objectParser
	 *            {@link HttpObjectParser} for the {@link HttpInputBuilder}.
	 */
	void addRequestObject(HttpObjectParser<?> objectParser);

	/**
	 * <p>
	 * Allows specifying a restricted set of supported response
	 * <code>Content-Type</code> values.
	 * <p>
	 * If none are specified, then will support all configured
	 * {@link HttpObjectResponderFactory} instances.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code>.
	 */
	void addResponseContentType(String contentType);

	/**
	 * Adds an {@link HttpObjectResponder} for the {@link HttpInputBuilder}.
	 * 
	 * @param objectResponder
	 *            {@link HttpObjectResponder} for the {@link HttpInputBuilder}.
	 */
	void addResponseObject(HttpObjectResponder<?> objectResponder);

	/**
	 * <p>
	 * Loads an annotation for the {@link HttpInputBuilder}.
	 * <p>
	 * This, for example, allows providing REST API definition information for
	 * the {@link HttpInputBuilder}.
	 * 
	 * @param annotation
	 *            Annotation for the {@link HttpInputBuilder}.
	 */
	void addAnnotation(Object annotation);

}