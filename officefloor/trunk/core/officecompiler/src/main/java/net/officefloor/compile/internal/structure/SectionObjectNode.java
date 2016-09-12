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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SubSectionObject;

/**
 * {@link SectionObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionObjectNode extends LinkObjectNode, SubSectionObject,
		SectionObject, OfficeSectionObject {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Section Object";

	/**
	 * Indicates if this {@link SectionObjectType} has been initialised.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

	/**
	 * Initialises this {@link SectionObjectType}.
	 * 
	 * @param objectType
	 *            Object type.
	 * @return <code>this</code> for builder pattern.
	 */
	SectionObjectNode initialise(String objectType);

	/**
	 * Obtains {@link SectionNode} containing this {@link SectionObjectNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link SectionObjectNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Loads the {@link SectionObjectType}.
	 * 
	 * @return {@link SectionObjectType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	SectionObjectType loadSectionObjectType();

}