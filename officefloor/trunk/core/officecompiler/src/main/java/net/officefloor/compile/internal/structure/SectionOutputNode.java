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
	@Deprecated // TODO use add method SectionOutputType loadSectionOutputType
	boolean isInitialised();

	/**
	 * Initialises this {@link SectionOutputType}.
	 * 
	 * @param argumentType
	 *            Argument type.
	 * @param isEscalationOnly
	 *            Flag indicating if escalation only.
	 */
	@Deprecated // TODO add method OfficeSectionOutputType loadOfficeSectionOutputType
	void initialise(String argumentType, boolean isEscalationOnly);

	/**
	 * Adds the context of the {@link Office} containing this
	 * {@link OfficeSectionOutput}.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	@Deprecated
	void addOfficeContext(String officeLocation);

	/**
	 * Obtains {@link SectionNode} containing this {@link SectionOutputNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link SectionOutputNode}.
	 */
	SectionNode getSectionNode();

}