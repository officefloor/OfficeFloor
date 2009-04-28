/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SectionObject} node.
 * 
 * @author Daniel
 */
public interface SectionObjectNode extends SectionObjectType, SubSectionObject,
		SectionObject, OfficeSectionObject, LinkObjectNode {

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
	 */
	void initialise(String objectType);

	/**
	 * Adds the context of the {@link Office} containing this
	 * {@link OfficeSectionObject}.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	void addOfficeContext(String officeLocation);

	/**
	 * Obtains {@link SectionNode} containing this {@link SectionObjectNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link SectionObjectNode}.
	 */
	SectionNode getSectionNode();

}