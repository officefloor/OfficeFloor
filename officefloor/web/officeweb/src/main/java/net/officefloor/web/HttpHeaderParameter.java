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

import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;
import net.officefloor.server.http.HttpHeader;

/**
 * {@link Annotation} to indicate the value is loaded from a {@link HttpHeader}
 * parameter.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Qualifier(nameFactory = HttpHeaderParameter.HttpHeaderParameterNameFactory.class)
public @interface HttpHeaderParameter {

	/**
	 * {@link QualifierNameFactory}.
	 */
	public static class HttpHeaderParameterNameFactory implements QualifierNameFactory<HttpHeaderParameter> {

		/**
		 * Obtains the qualifier from name.
		 * 
		 * @param name Name.
		 * @return Qualifier.
		 */
		public static String getQualifier(String name) {
			return HttpHeaderParameter.class.getSimpleName() + "_" + name;
		}

		/*
		 * ================== QualifierNameFactory =======================
		 */

		@Override
		public String getQualifierName(HttpHeaderParameter annotation) {
			return getQualifier(annotation.value());
		}
	}

	/**
	 * Name of parameter.
	 * 
	 * @return Name of parameter.
	 */
	String value();

}
