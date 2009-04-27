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
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.DoubleKeyMap;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * {@link SectionSource} for a {@link SectionModel}.
 * 
 * @author Daniel
 */
public class SectionModelSectionSource extends AbstractSectionSource implements
		SectionSourceService {

	/*
	 * ==================== SectionSourceService ============================
	 */

	@Override
	public String getSectionSourceAlias() {
		return "SECTION";
	}

	@Override
	public Class<? extends SectionSource> getSectionSourceClass() {
		return this.getClass();
	}

	/*
	 * ================== SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer,
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
					designer.addSectionInput(inputName, input
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
			designer.addSectionOutput(extFlow.getExternalFlowName(), extFlow
					.getArgumentType(), isEscalationOnly);
		}

		// Add the external managed objects as objects required by section
		for (ExternalManagedObjectModel extMo : section
				.getExternalManagedObjects()) {
			designer.addSectionObject(extMo.getExternalManagedObjectName(),
					extMo.getObjectType());
		}

		// Add the sub sections, keeping registry of sub sections/inputs
		Map<String, SubSection> subSections = new HashMap<String, SubSection>();
		DoubleKeyMap<String, String, SubSectionInput> subSectionInputs = new DoubleKeyMap<String, String, SubSectionInput>();
		for (SubSectionModel subSectionModel : section.getSubSections()) {

			// Add the sub section and register
			String subSectionName = subSectionModel.getSubSectionName();
			SubSection subSection = designer.addSubSection(subSectionName,
					subSectionModel.getSectionSourceClassName(),
					subSectionModel.getSectionLocation());
			subSections.put(subSectionName, subSection);
			for (PropertyModel property : subSectionModel.getProperties()) {
				subSection.addProperty(property.getName(), property.getValue());
			}

			// Add the sub section inputs and register
			for (SubSectionInputModel inputModel : subSectionModel
					.getSubSectionInputs()) {
				String inputName = inputModel.getSubSectionInputName();
				SubSectionInput input = subSection
						.getSubSectionInput(inputName);
				subSectionInputs.put(subSectionName, inputName, input);
			}
		}

		// Add the sub section outputs now that all inputs available
		for (SubSectionModel subSectionModel : section.getSubSections()) {

			// Obtain the sub section
			String subSectionName = subSectionModel.getSubSectionName();
			SubSection subSection = subSections.get(subSectionName);

			// Add the sub section outputs
			for (SubSectionOutputModel outputModel : subSectionModel
					.getSubSectionOutputs()) {

				// Add the sub section output
				String outputName = outputModel.getSubSectionOutputName();
				SubSectionOutput output = subSection
						.getSubSectionOutput(outputName);

				// Determine if link to a sub section input
				SubSectionInput linkedInput = null;
				SubSectionOutputToSubSectionInputModel outputToInput = outputModel
						.getSubSectionInput();
				if (outputToInput != null) {
					SubSectionInputModel inputModel = outputToInput
							.getSubSectionInput();
					if (inputModel != null) {
						SubSectionModel linkedSubSection = this
								.getSubSectionForInput(section, inputModel);
						linkedInput = subSectionInputs.get(linkedSubSection
								.getSubSectionName(), inputModel
								.getSubSectionInputName());
					}
				}
				if (linkedInput != null) {
					// Link the output to the input
					designer.link(output, linkedInput);
				}
			}
		}
	}

	/**
	 * Obtains the {@link SubSectionModel} containing the input
	 * {@link SubSectionInputModel}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param input
	 *            {@link SubSectionInputModel}.
	 * @return {@link SubSectionModel}.
	 */
	private SubSectionModel getSubSectionForInput(SectionModel section,
			SubSectionInputModel input) {

		// Search the for input (which is contained in the target sub section)
		for (SubSectionModel subSection : section.getSubSections()) {
			for (SubSectionInputModel check : subSection.getSubSectionInputs()) {
				if (check == input) {
					// Found the input and subsequently the containing sub
					// section
					return subSection;
				}
			}
		}

		// No sub section if at this point
		return null;
	}

}