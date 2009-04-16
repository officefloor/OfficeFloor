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

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Node within the hierarchy of {@link OfficeSection} instances.
 * 
 * @author Daniel
 */
public interface SectionNode extends SectionBuilder, SectionType, SubSection,
		OfficeSection {

	// TODO need to add initialise and isInitialised for getting input

	/**
	 * Obtains the {@link DeployedOfficeInput}.
	 * 
	 * @param inputName
	 *            Input name as per the {@link OfficeInputType}.
	 * @return {@link DeployedOfficeInput}.
	 */
	DeployedOfficeInput getDeployedOfficeInput(String inputName);

	/**
	 * Loads the {@link OfficeSection} of this {@link SectionNode} and all its
	 * {@link SubSection} {@link SectionNode} instances.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeSection}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	void loadSection(String officeLocation,
			ConfigurationContext configurationContext, ClassLoader classLoader);

}