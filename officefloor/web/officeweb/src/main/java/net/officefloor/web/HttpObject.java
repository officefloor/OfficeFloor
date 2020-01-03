/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.build.HttpObjectParser;

/**
 * {@link Annotation} on the {@link Class} of the parameter (or the parameter
 * itself) to indicate it is parsed out of the {@link HttpRequest} via an
 * {@link HttpObjectParser}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpObject {

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
