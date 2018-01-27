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
package net.officefloor.plugin.managedfunction.clazz;

import java.lang.annotation.Annotation;

/**
 * Determines the {@link Qualifier} name from the {@link Qualifier} attributes.
 * 
 * @author Daniel Sagenschneider
 */
public interface QualifierNameFactory<A extends Annotation> {

	/**
	 * Obtains the {@link Qualifier} name from the {@link Annotation}.
	 * 
	 * @param annotation
	 *            {@link Annotation} containing attributes to aid determining
	 *            the name.
	 * @return Qualified name.
	 */
	String getQualifierName(A annotation);

}