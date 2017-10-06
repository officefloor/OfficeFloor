/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.state.HttpRequestState;

/**
 * Annotation on the {@link Class} of the parameter to indicate it is parsed out
 * of the {@link HttpRequest} via an {@link HttpObjectParser}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpObject {

	/**
	 * Allows specifying the name to bind the object into the
	 * {@link HttpRequestState}.
	 * 
	 * @return Name to bind the object into the {@link HttpRequestState}. The
	 *         blank default value indicates for the {@link ManagedObject} to
	 *         assign its own unique value.
	 */
	String bind() default "";

	/**
	 * <p>
	 * Leaving an empty array will provide all <code>Content-Type</code> parsing
	 * available for the {@link Object}.
	 * <p>
	 * This is provided to restrict <code>Content-Type</code> parsing.
	 * 
	 * @return Restricted list of <code>Content-Type</code> parsing available
	 *         for the {@link Object}.
	 */
	String[] acceptedContentTypes() default {};

}