/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SectionOutput} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionOutputNode extends SectionOutputType, SectionOutput,
		SubSectionOutput, OfficeSectionOutput, LinkFlowNode {

	/**
	 * Indicates if this {@link SectionOutputType} has been initialised.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

	/**
	 * Initialises this {@link SectionOutputType}.
	 * 
	 * @param argumentType
	 *            Argument type.
	 * @param isEscalationOnly
	 *            Flag indicating if escalation only.
	 */
	void initialise(String argumentType, boolean isEscalationOnly);

	/**
	 * Adds the context of the {@link Office} containing this
	 * {@link OfficeSectionOutput}.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	void addOfficeContext(String officeLocation);

	/**
	 * Obtains {@link SectionNode} containing this {@link SectionOutputNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link SectionOutputNode}.
	 */
	SectionNode getSectionNode();

}