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
package net.officefloor.model.impl.desk;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.Section;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.section.source.SectionTypeBuilder;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link SectionSource} for a {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskModelSectionSource implements SectionSource {

	/**
	 * Sources the {@link SectionType} from the {@link DeskModel}.
	 * 
	 * @param desk
	 *            {@link DeskModel}.
	 * @param sectionTypeBuilder
	 *            {@link SectionTypeBuilder} to populate the {@link SectionType}
	 *            for the {@link DeskModel}.
	 */
	public static void sourceSectionType(DeskModel desk,
			SectionTypeBuilder sectionTypeBuilder) {

		// Add the public tasks as inputs
		for (TaskModel task : desk.getTasks()) {
			if (task.getIsPublic()) {
				// TODO determine parameter type from work task
				String parameterType = null;
				sectionTypeBuilder.addInput(task.getTaskName(), parameterType);
			}
		}

		// Add the external flows as outputs
		for (ExternalFlowModel extFlow : desk.getExternalFlows()) {
			// TODO determine if escalation only
			boolean isEscalationOnly = false;
			sectionTypeBuilder.addOutput(extFlow.getExternalFlowName(), extFlow
					.getArgumentType(), isEscalationOnly);
		}

		// Add the external managed objects as objects
		for (ExternalManagedObjectModel extMo : desk
				.getExternalManagedObjects()) {
			sectionTypeBuilder.addObject(extMo.getExternalManagedObjectName(),
					extMo.getObjectType());
		}
	}

	/*
	 * ================= SectionSource ===========================
	 */

	@Override
	public SectionSourceSpecification getSpecification() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionSource.getSpecification");
	}

	@Override
	public void sourceSectionType(SectionTypeBuilder sectionTypeBuilder,
			SectionSourceContext context) throws Exception {

		// Obtain the configuration to the desk
		ConfigurationItem configuration = context.getConfiguration(context
				.getSectionLocation());

		// Retrieve the desk model
		DeskModel desk = new DeskRepositoryImpl(new ModelRepositoryImpl())
				.retrieveDesk(configuration);

		// Source the section type for the desk
		sourceSectionType(desk, sectionTypeBuilder);
	}

	@Override
	public Section sourceSection(SectionSourceContext context) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionSource.sourceSection");
	}

}