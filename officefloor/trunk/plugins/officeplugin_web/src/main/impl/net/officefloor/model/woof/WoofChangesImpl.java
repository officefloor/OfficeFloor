/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.model.woof;

import java.util.Map;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;

/**
 * {@link Change} for the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofChangesImpl implements WoofChanges {

	/**
	 * {@link WoofModel}.
	 */
	private final WoofModel model;

	/**
	 * Initiate.
	 * 
	 * @param model
	 *            {@link WoofModel} to change.
	 */
	public WoofChangesImpl(WoofModel model) {
		this.model = model;
	}

	/*
	 * ======================= WoofChanges =======================
	 */

	@Override
	public Change<WoofTemplateModel> addTemplate(OfficeSection section,
			String templatePath, String templateLogicClass, String uri) {

		// Create the template
		String templateName = section.getOfficeSectionName();
		final WoofTemplateModel template = new WoofTemplateModel(templateName,
				uri, "example/Template.ofp", templateLogicClass);

		// Add the outputs for the template
		for (OfficeSectionOutput output : section.getOfficeSectionOutputs()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Add the Woof Template Output
			String outputName = output.getOfficeSectionOutputName();
			String argumentType = output.getArgumentType();
			template.addOutput(new WoofTemplateOutputModel(outputName,
					argumentType));
		}

		// Return change to add template
		return new AbstractChange<WoofTemplateModel>(template, "Add Template") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofTemplate(template);
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofTemplate(template);
			}
		};
	}

	@Override
	public Change<WoofTemplateModel> removeTemplate(WoofTemplateModel template) {
		// TODO implement WoofChanges.removeTemplate
		throw new UnsupportedOperationException(
				"TODO implement WoofChanges.removeTemplate");
	}

	@Override
	public Change<WoofSectionModel> addSection(OfficeSection section,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, Map<String, String> inputToUri) {

		// Create the section
		String sectionName = section.getOfficeSectionName();
		final WoofSectionModel woofSection = new WoofSectionModel(sectionName,
				sectionSourceClassName, sectionLocation);

		// Add the properties
		for (Property property : properties) {
			woofSection.addProperty(new PropertyModel(property.getName(),
					property.getValue()));
		}

		// Add the inputs
		for (OfficeSectionInput input : section.getOfficeSectionInputs()) {
			String inputName = input.getOfficeSectionInputName();
			String parameterType = input.getParameterType();
			String uri = inputToUri.get(inputName);
			woofSection.addInput(new WoofSectionInputModel(inputName,
					parameterType, uri));
		}

		// Add the outputs
		for (OfficeSectionOutput output : section.getOfficeSectionOutputs()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Add the output
			String outputName = output.getOfficeSectionOutputName();
			String argumentType = output.getArgumentType();
			woofSection.addOutput(new WoofSectionOutputModel(outputName,
					argumentType));
		}

		// Return the change to add section
		return new AbstractChange<WoofSectionModel>(woofSection, "Add Section") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofSection(woofSection);
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofSection(woofSection);
			}
		};
	}

	@Override
	public Change<WoofResourceModel> addResource(String resourceName,
			String resourcePath) {

		// Create the resource
		final WoofResourceModel resource = new WoofResourceModel(resourceName,
				resourcePath);

		// Return change to add resource
		return new AbstractChange<WoofResourceModel>(resource, "Add Resource") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofResource(resource);
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofResource(resource);
			}
		};
	}

	@Override
	public Change<WoofExceptionModel> addException(String exceptionClassName) {

		// Create the exception
		final WoofExceptionModel exception = new WoofExceptionModel(
				exceptionClassName);

		// Return change to add exception
		return new AbstractChange<WoofExceptionModel>(exception,
				"Add Exception") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofException(exception);
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofException(exception);
			}
		};
	}

}