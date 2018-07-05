/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.officefloor.plugin.managedfunction.clazz.Qualifier;
import net.officefloor.plugin.managedfunction.clazz.QualifierNameFactory;

/**
 * {@link Annotation} to indicate the value is loaded from a path parameter.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Qualifier(nameFactory = HttpPathParameter.HttpPathParameterNameFactory.class)
public @interface HttpPathParameter {

	/**
	 * {@link QualifierNameFactory}.
	 */
	public static class HttpPathParameterNameFactory implements QualifierNameFactory<HttpPathParameter> {
		@Override
		public String getQualifierName(HttpPathParameter annotation) {
			return HttpPathParameter.class.getSimpleName() + "_" + annotation.value();
		}
	}

	/**
	 * Allows specifying the path parameter name.
	 * 
	 * @return Path parameter name.
	 */
	String value();

}