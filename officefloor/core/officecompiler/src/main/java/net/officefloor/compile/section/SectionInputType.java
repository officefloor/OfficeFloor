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
package net.officefloor.compile.section;

import net.officefloor.compile.type.AnnotatedType;

/**
 * <code>Type definition</code> of an input for a {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionInputType extends AnnotatedType {

	/**
	 * Obtains the name of this {@link SectionInputType}.
	 * 
	 * @return Name of this {@link SectionInputType}.
	 */
	String getSectionInputName();

	/**
	 * <p>
	 * Obtains the fully qualified {@link Class} name of the parameter type for this
	 * {@link SectionInputType}.
	 * <p>
	 * The name is returned rather than the actual {@link Class} to enable the
	 * {@link SectionType} to be obtained should the {@link Class} not be available
	 * to the {@link ClassLoader}.
	 * 
	 * @return Fully qualified {@link Class} name of the parameter type.
	 */
	String getParameterType();

}