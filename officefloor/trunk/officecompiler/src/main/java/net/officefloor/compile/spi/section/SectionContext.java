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
package net.officefloor.compile.spi.section;

import net.officefloor.compile.spi.office.source.OfficeSection;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;

/**
 * Context for the {@link OfficeSection}.
 * 
 * @author Daniel
 */
// TODO transfer functionality to SectionBuilder
@Deprecated
public interface SectionContext {

	// sectionInput -> task, subSectionInput, sectionOutput

	// taskFlow -> task, subSectionInput, sectionOutput

	void link(TaskFlow taskFlow, SectionTask task,
			FlowInstigationStrategyEnum instigationStrategy);

	void link(TaskFlow taskFlow, SubSectionInput subSectionInput,
			FlowInstigationStrategyEnum instigationStrategy);

	void link(TaskFlow taskFlow, SectionOutput sectionOutput,
			FlowInstigationStrategyEnum instigationStrategy);

	// task (next) -> task, subSectionInput, sectionOutput

	void link(SectionTask task, SectionTask nextTask);

	void link(SectionTask task, SubSectionInput subSectionInput);

	void link(SectionTask task, SectionOutput sectionOutput);

	// subSectionOutput -> task, subSectionInput, sectionOutput

	void link(SubSectionOutput subSectionOutput, SectionTask task);

	void link(SubSectionOutput subSectionOutput, SubSectionInput subSectionInput);

	void link(SubSectionOutput subSectionOutput, SectionOutput sectionOutput);

	// taskObject -> sectionObject

	void link(TaskObject taskObject, SectionObject sectionObject);

	// subSectionObject -> sectionObject

	void link(SubSectionObject subSectionObject, SectionObject sectionObject);

}