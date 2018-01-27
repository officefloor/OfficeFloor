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
package net.officefloor.web.template.section;

import net.officefloor.web.HttpInputPath;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Annotation identifying a redirect to the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateRedirectAnnotation {

	/**
	 * Type provided to the redirect to source values to construct the
	 * {@link HttpInputPath}.
	 */
	private final Class<?> valuesType;

	/**
	 * Instantiate.
	 * 
	 * @param valuesType
	 *            Type provided to the redirect to source values to construct
	 *            the {@link HttpInputPath}. May be <code>null</code> if no
	 *            values are required.
	 */
	public WebTemplateRedirectAnnotation(Class<?> valuesType) {
		this.valuesType = valuesType;
	}

	/**
	 * Obtains the type provided to the redirect to source values to construct
	 * the {@link HttpInputPath}.
	 * 
	 * @return Type provided to the redirect to source values to construct the
	 *         {@link HttpInputPath}. May be <code>null</code>.
	 */
	public Class<?> getValuesType() {
		return this.valuesType;
	}

}