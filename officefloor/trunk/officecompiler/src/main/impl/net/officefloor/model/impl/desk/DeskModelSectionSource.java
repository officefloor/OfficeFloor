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

import java.io.FileNotFoundException;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link SectionSource} for a {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskModelSectionSource extends AbstractSectionSource {

	/*
	 * ================= SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceSection(SectionDesigner sectionBuilder,
			SectionSourceContext context) throws Exception {

		// Obtain the configuration to the desk
		ConfigurationItem configuration = context.getConfiguration(context
				.getSectionLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find desk '"
					+ context.getSectionLocation() + "'");
		}

		// Retrieve the desk model
		DeskModel desk = new DeskRepositoryImpl(new ModelRepositoryImpl())
				.retrieveDesk(configuration);

		// Add the public tasks as inputs
		for (TaskModel task : desk.getTasks()) {
			if (task.getIsPublic()) {

				// Obtain the work task
				WorkTaskModel workTask = task.getWorkTask().getWorkTask();

				// Determine the parameter type from the work task
				String parameterType = null;
				for (WorkTaskObjectModel taskObject : workTask.getTaskObjects()) {
					if (taskObject.getIsParameter()) {
						// TODO handle two parameters to work for a desk
						parameterType = taskObject.getObjectType();
					}
				}

				// Add the section input
				sectionBuilder.addSectionInput(task.getTaskName(),
						parameterType);
			}
		}

		// Add the external flows as outputs
		for (ExternalFlowModel extFlow : desk.getExternalFlows()) {

			// Determine if escalation only (only has task escalation connected)
			boolean isEscalationOnly = ((extFlow.getPreviousTasks().size() == 0)
					&& (extFlow.getTaskFlows().size() == 0) && (extFlow
					.getTaskEscalations().size() > 0));

			// Add the section output
			sectionBuilder.addSectionOutput(extFlow.getExternalFlowName(),
					extFlow.getArgumentType(), isEscalationOnly);
		}

		// Add the external managed objects as objects
		for (ExternalManagedObjectModel extMo : desk
				.getExternalManagedObjects()) {
			sectionBuilder.addSectionObject(extMo
					.getExternalManagedObjectName(), extMo.getObjectType());
		}
	}

}