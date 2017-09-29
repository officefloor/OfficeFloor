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

import net.officefloor.server.http.HttpMethod;

/**
 * Input details for the application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpInput {

	/**
	 * Indicates if the input is secure.
	 * 
	 * @return <code>true</code> if secure.
	 */
	boolean isSecure();

	/**
	 * Obtains the {@link HttpMethod} for the input.
	 * 
	 * @return {@link HttpMethod}.
	 */
	HttpMethod getHttpMethod();

	/**
	 * Obtains the application path for the input.
	 * 
	 * @return Application path.
	 */
	String getApplicationPath();

	/**
	 * Adds an {@link ObjectResponder} for the {@link HttpInput}.
	 * 
	 * @param objectResponder
	 *            {@link ObjectResponder} for the {@link HttpInput}.
	 */
	void addObjectResponder(ObjectResponder<?> objectResponder);

	/**
	 * Obtains the configured {@link ObjectResponder} instances.
	 * 
	 * @return Configured {@link ObjectResponder} instances.
	 */
	ObjectResponder<?>[] getObjectResponders();

	/**
	 * <p>
	 * Loads an annotation for the {@link HttpInput}.
	 * <p>
	 * This, for example, allows providing REST API definition information for
	 * the {@link HttpInput}.
	 * 
	 * @param annotation
	 *            Annotation for the {@link HttpInput}.
	 */
	void addAnnotation(Object annotation);

	/**
	 * Obtains the annotations for this {@link HttpInput}.
	 * 
	 * @return Annotations for this {@link HttpInput}.
	 */
	Object[] getAnnotations();

}