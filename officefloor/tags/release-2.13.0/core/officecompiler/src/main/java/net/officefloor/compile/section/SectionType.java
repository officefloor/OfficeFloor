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
package net.officefloor.compile.section;

import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of a section of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionType {

	/**
	 * Obtains the {@link SectionInputType} definitions for the inputs into the
	 * {@link SectionType}.
	 * 
	 * @return {@link SectionInputType} definitions for the inputs into the
	 *         {@link SectionType}.
	 */
	SectionInputType[] getSectionInputTypes();

	/**
	 * Obtains the {@link SectionOutputType} definitions for the outputs from
	 * the {@link SectionType}.
	 * 
	 * @return {@link SectionOutputType} definitions for the outputs from the
	 *         {@link SectionType}.
	 */
	SectionOutputType[] getSectionOutputTypes();

	/**
	 * Obtains the {@link SectionObjectType} definitions for the {@link Object}
	 * dependencies required by the {@link SectionType}.
	 * 
	 * @return {@link SectionObjectType} definitions for the {@link Object}
	 *         dependencies required by the {@link SectionType}.
	 */
	SectionObjectType[] getSectionObjectTypes();

}