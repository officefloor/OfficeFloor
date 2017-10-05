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
package net.officefloor.web;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.web.cookie.HttpCookie;

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
	 * Adds an {@link HttpEntityArgumentParser} for the
	 * {@link HttpInputBuilder}.
	 * 
	 * @param argumentParser
	 *            {@link HttpEntityArgumentParser} for the
	 *            {@link HttpInputBuilder}.
	 */
	HttpEntityParametersBuilder addEntityParameters(HttpEntityArgumentParser argumentParser);

	/**
	 * Adds an {@link HttpEntityObjectParser} for the {@link HttpInputBuilder}.
	 * 
	 * @param objectParser
	 *            {@link HttpEntityObjectParser} for the
	 *            {@link HttpInputBuilder}.
	 */
	void addRequestObject(HttpEntityObjectParser<?> objectParser);

	/**
	 * Adds an {@link ObjectResponder} for the {@link HttpInputBuilder}.
	 * 
	 * @param objectResponder
	 *            {@link ObjectResponder} for the {@link HttpInputBuilder}.
	 */
	void addResponseObject(ObjectResponder<?> objectResponder);

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