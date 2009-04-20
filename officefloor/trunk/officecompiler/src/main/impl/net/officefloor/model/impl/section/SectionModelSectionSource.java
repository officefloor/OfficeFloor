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
package net.officefloor.model.impl.section;

import java.io.FileNotFoundException;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;

/**
 * {@link SectionSource} for a {@link SectionModel}.
 * 
 * @author Daniel
 */
public class SectionModelSectionSource extends AbstractSectionSource {

	/*
	 * ================== SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner sectionDesigner,
			SectionSourceContext context) throws Exception {

		// Obtain the configuration to the section
		ConfigurationItem configuration = context.getConfiguration(context
				.getSectionLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find section '"
					+ context.getSectionLocation() + "'");
		}

		// Retrieve the section model
		SectionModel section = new SectionRepositoryImpl(
				new ModelRepositoryImpl()).retrieveSection(configuration);

		// Add the public section inputs as inputs to section
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
				if (input.getIsPublic()) {
					// Obtain the public name for the input
					String inputName = input.getPublicInputName();
					inputName = (CompileUtil.isBlank(inputName) ? input
							.getSubSectionInputName() : inputName);

					// Add the input
					sectionDesigner.addSectionInput(inputName, input
							.getParameterType());
				}
			}
		}

		// Add the external flows as outputs from the section
		for (ExternalFlowModel extFlow : section.getExternalFlows()) {

			// Determine if all connected sub section outputs are escalation
			boolean isEscalationOnly = true;
			for (SubSectionOutputToExternalFlowModel conn : extFlow
					.getSubSectionOutputs()) {
				if (!conn.getSubSectionOutput().getEscalationOnly()) {
					// Connected to output which is not escalation only
					isEscalationOnly = false;
				}
			}

			// Add the output
			sectionDesigner.addSectionOutput(extFlow.getExternalFlowName(),
					extFlow.getArgumentType(), isEscalationOnly);
		}

		// Add the external managed objects as objects required by section
		for (ExternalManagedObjectModel extMo : section
				.getExternalManagedObjects()) {
			sectionDesigner.addSectionObject(extMo
					.getExternalManagedObjectName(), extMo.getObjectType());
		}
	}

}