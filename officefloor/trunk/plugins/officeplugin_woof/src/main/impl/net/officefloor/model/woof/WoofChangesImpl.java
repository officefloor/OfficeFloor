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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.AggregateChange;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.plugin.comet.web.http.section.CometHttpTemplateSectionExtension;
import net.officefloor.plugin.gwt.module.GwtChanges;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.woof.comet.CometWoofTemplateExtensionService;
import net.officefloor.plugin.woof.gwt.GwtWoofTemplateExtensionService;

/**
 * {@link Change} for the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofChangesImpl implements WoofChanges {

	/**
	 * Name of the property for the GWT Module path.
	 */
	private static final String PROPERTY_GWT_MODULE_PATH = "gwt.module.path";

	/**
	 * {@link WoofTemplateModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofTemplateModel> TEMPLATE_NAME_EXTRACTOR = new NameExtractor<WoofTemplateModel>() {
		@Override
		public String extractName(WoofTemplateModel model) {
			return model.getWoofTemplateName();
		}
	};

	/**
	 * {@link WoofSectionModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofSectionModel> SECTION_NAME_EXTRACTOR = new NameExtractor<WoofSectionModel>() {
		@Override
		public String extractName(WoofSectionModel model) {
			return model.getWoofSectionName();
		}
	};

	/**
	 * {@link WoofGovernanceModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofGovernanceModel> GOVERNANCE_NAME_EXTRACTOR = new NameExtractor<WoofGovernanceModel>() {
		@Override
		public String extractName(WoofGovernanceModel model) {
			return model.getWoofGovernanceName();
		}
	};

	/**
	 * {@link WoofGovernanceAreaModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofGovernanceAreaModel> GOVERNANCE_AREA_IDENTIFIER_EXTRACTOR = new NameExtractor<WoofGovernanceAreaModel>() {
		@Override
		public String extractName(WoofGovernanceAreaModel model) {
			return model.getWidth() + "-" + model.getHeight();
		}
	};

	/**
	 * {@link WoofResourceModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofResourceModel> RESOURCE_NAME_EXTRACTOR = new NameExtractor<WoofResourceModel>() {
		@Override
		public String extractName(WoofResourceModel model) {
			return model.getWoofResourceName();
		}
	};

	/**
	 * {@link WoofExceptionModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofExceptionModel> EXCEPTION_NAME_EXTRACTOR = new NameExtractor<WoofExceptionModel>() {
		@Override
		public String extractName(WoofExceptionModel model) {
			return model.getClassName();
		}
	};

	/**
	 * Sorts the models by name.
	 * 
	 * @param models
	 *            Models.
	 * @param nameExtractor
	 *            {@link NameExtractor}.
	 */
	private static <M> void sortByName(List<M> models,
			final NameExtractor<M> nameExtractor) {
		Collections.sort(models, new Comparator<M>() {
			@Override
			public int compare(M a, M b) {
				String nameA = nameExtractor.extractName(a);
				String nameB = nameExtractor.extractName(b);
				return String.CASE_INSENSITIVE_ORDER.compare(nameA, nameB);
			}
		});
	}

	/**
	 * Obtains the unique name.
	 * 
	 * @param name
	 *            Base name.
	 * @param model
	 *            Model being named. May be <code>null</code>.
	 * @param models
	 *            Listing of the existing models.
	 * @param nameExtractor
	 *            {@link NameExtractor}.
	 * @return Unique name.
	 */
	private static <M> String getUniqueName(final String name, M model,
			List<M> models, NameExtractor<M> nameExtractor) {

		// Determine suffix
		String uniqueName = name;
		int suffix = 1;
		boolean isNameExist = false; // first time not include suffix
		do {
			// Increment suffix should name exist
			if (isNameExist) {
				suffix++;
				uniqueName = name + "-" + suffix;
			}

			// Check if name already exists
			isNameExist = false;
			for (M check : models) {
				if (check == model) {
					continue; // ignore same model
				}
				String extractedName = nameExtractor.extractName(check);
				if (uniqueName.equals(extractedName)) {
					isNameExist = true;
				}
			}
		} while (isNameExist);

		// Return the unique name
		return uniqueName;
	}

	/**
	 * Extracts the name from the model.
	 */
	private static interface NameExtractor<M> {

		/**
		 * Obtains the name from the model.
		 * 
		 * @param model
		 *            Model.
		 * @return Name of the model.
		 */
		String extractName(M model);
	}

	/**
	 * Obtains the {@link WoofTemplateModel} name.
	 * 
	 * @param templatePath
	 *            Template Path.
	 * @param uri
	 *            URI.
	 * @param template
	 *            {@link WoofTemplateModel}. May be <code>null</code>.
	 * @param templates
	 *            {@link WoofTemplateModel} instances.
	 * @return Unique {@link WoofTemplateModel} name.
	 */
	private static String getTemplateName(String templatePath, String uri,
			WoofTemplateModel template, List<WoofTemplateModel> templates) {

		// Obtain the base template name
		String templateName = uri;
		if (CompileUtil.isBlank(templateName)) {
			// Use simple name from template path
			templateName = templatePath;
			int index = templateName.lastIndexOf('/');
			if (index >= 0) {
				templateName = templateName.substring(index + "/".length());
			}
			index = templateName.indexOf('.');
			if (index > 0) {
				templateName = templateName.substring(0, index);
			}
		}

		// Obtain the unique template name
		templateName = getUniqueName(templateName, template, templates,
				TEMPLATE_NAME_EXTRACTOR);

		// Return the template name
		return templateName;
	}

	/**
	 * Removes the {@link ConnectionModel}.
	 * 
	 * @param connection
	 *            {@link ConnectionModel} to remove. May be <code>null</code> if
	 *            nothing to remove.
	 * @param list
	 *            List to add the removed {@link ConnectionModel} instances.
	 */
	private static void removeConnection(ConnectionModel connection,
			List<ConnectionModel> list) {

		// Ensure have connection to remove
		if (connection == null) {
			return;
		}

		// Remove the connection
		connection.remove();
		list.add(connection);
	}

	/**
	 * Removes the {@link ConnectionModel} instances.
	 * 
	 * @param connections
	 *            Listing of {@link ConnectionModel} instances to remove. May be
	 *            <code>null</code> if nothing to remove.
	 * @param list
	 *            List to add the removed {@link ConnectionModel} instances.
	 */
	private static <C extends ConnectionModel> void removeConnections(
			List<C> connections, List<ConnectionModel> list) {

		// Ensure have connections
		if (connections == null) {
			return;
		}

		// Remove the connections
		for (C conn : new ArrayList<C>(connections)) {
			removeConnection(conn, list);
		}
	}

	/**
	 * Reconnect the {@link ConnectionModel} instances.
	 * 
	 * @param connections
	 *            {@link ConnectionModel} instances to reconnect. May be
	 *            <code>null</code> if nothing to reconnect.
	 */
	private static <C extends ConnectionModel> void reconnectConnections(
			C[] connections) {

		// Ensure have connections
		if (connections == null) {
			return;
		}

		// Re-connect
		for (int i = 0; i < connections.length; i++) {
			connections[i].connect();
		}
	}

	/**
	 * {@link WoofModel}.
	 */
	private final WoofModel model;

	/**
	 * {@link GwtChanges}.
	 */
	private final GwtChanges gwtChanges;

	/**
	 * Initiate.
	 * 
	 * @param model
	 *            {@link WoofModel} to change.
	 * @param gwtChanges
	 *            {@link GwtChanges}.
	 */
	public WoofChangesImpl(WoofModel model, GwtChanges gwtChanges) {
		this.model = model;
		this.gwtChanges = gwtChanges;
	}

	/**
	 * Sorts the {@link WoofTemplateModel} instances.
	 */
	private void sortTemplates() {
		sortByName(this.model.getWoofTemplates(), TEMPLATE_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofSectionModel} instances.
	 */
	private void sortSections() {
		sortByName(this.model.getWoofSections(), SECTION_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofGovernanceModel} instances.
	 */
	private void sortGovernances() {
		sortByName(this.model.getWoofGovernances(), GOVERNANCE_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofGovernanceAreaModel} for the
	 * {@link WoofGovernanceModel}.
	 * 
	 * @param governance
	 *            {@link WoofGovernanceModel} to have its
	 *            {@link WoofGovernanceAreaModel} instances sorted.
	 */
	private void sortGovernanceAreas(WoofGovernanceModel governance) {
		sortByName(governance.getGovernanceAreas(),
				GOVERNANCE_AREA_IDENTIFIER_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofResourceModel} instances.
	 */
	private void sortResources() {
		sortByName(this.model.getWoofResources(), RESOURCE_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofExceptionModel} instances.
	 */
	private void sortExceptions() {
		sortByName(this.model.getWoofExceptions(), EXCEPTION_NAME_EXTRACTOR);
	}

	/**
	 * Obtains the {@link WoofSectionModel} for the
	 * {@link WoofSectionInputModel}.
	 * 
	 * @param input
	 *            {@link WoofSectionInputModel}.
	 * @return {@link WoofSectionModel} containing the
	 *         {@link WoofSectionInputModel} or <code>null</code> if not within
	 *         {@link WoofModel}.
	 */
	public WoofSectionModel getSection(WoofSectionInputModel input) {

		// Find the containing section
		WoofSectionModel containingSection = null;
		for (WoofSectionModel section : this.model.getWoofSections()) {
			for (WoofSectionInputModel check : section.getInputs()) {
				if (check == input) {
					// Found containing section
					containingSection = section;
				}
			}
		}

		// Return the containing section
		return containingSection;
	}

	/**
	 * Obtains the {@link WoofGovernanceModel} for the
	 * {@link WoofGovernanceAreaModel}.
	 * 
	 * @param area
	 *            {@link WoofGovernanceAreaModel}.
	 * @return {@link WoofGovernanceModel} for the
	 *         {@link WoofGovernanceAreaModel} or <code>null</code> if not
	 *         within the {@link WoofModel}.
	 */
	private WoofGovernanceModel getGovernance(WoofGovernanceAreaModel area) {

		// Find the containing governance
		WoofGovernanceModel containingGovernance = null;
		for (WoofGovernanceModel governance : this.model.getWoofGovernances()) {
			for (WoofGovernanceAreaModel check : governance
					.getGovernanceAreas()) {
				if (check == area) {
					// Found containing governance
					containingGovernance = governance;
				}
			}
		}

		// Return the containing governance
		return containingGovernance;
	}

	/*
	 * ======================= WoofChanges =======================
	 */

	@Override
	public Change<WoofTemplateModel> addTemplate(String templatePath,
			String templateLogicClass, SectionType section, String uri,
			String gwtEntryPointClassName,
			String[] gwtServiceAsyncInterfaceNames, boolean isEnableComet,
			String cometManualPublishMethodName) {

		// Obtain the template name
		String templateName = getTemplateName(templatePath, uri, null,
				this.model.getWoofTemplates());

		// Create the template
		final WoofTemplateModel template = new WoofTemplateModel(templateName,
				uri, templatePath, templateLogicClass);

		// Add the outputs for the template
		for (SectionOutputType output : section.getSectionOutputTypes()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Add the Woof Template Output
			String outputName = output.getSectionOutputName();
			String argumentType = output.getArgumentType();
			template.addOutput(new WoofTemplateOutputModel(outputName,
					argumentType));
		}

		// Return change to add template
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(
				template, "Add Template") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofTemplate(template);
				WoofChangesImpl.this.sortTemplates();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofTemplate(template);
			}
		};

		// Determine if have GWT Extension
		if ((gwtEntryPointClassName != null)
				&& (gwtEntryPointClassName.trim().length() > 0)) {

			// Add the GWT Extension
			WoofTemplateExtensionModel gwtExtension = new WoofTemplateExtensionModel(
					GwtWoofTemplateExtensionService.EXTENSION_ALIAS);
			template.addExtension(gwtExtension);

			// Create the GWT Module
			GwtModuleModel module = new GwtModuleModel(uri,
					gwtEntryPointClassName, null);

			// Include inherits for Comet (if using)
			if (isEnableComet) {
				// Extend GWT Module for Comet
				CometHttpTemplateSectionExtension.extendGwtModule(module);
			}

			// Add property for the GWT Module path
			String gwtModulePath = this.gwtChanges.createGwtModulePath(module);
			gwtExtension.addProperty(new PropertyModel("gwt.module.path",
					gwtModulePath));

			// Include change for adding GWT Module
			Change<?> gwtChange = this.gwtChanges.updateGwtModule(module, null);

			// Create aggregate change to include GWT changes
			change = new AggregateChange<WoofTemplateModel>(change.getTarget(),
					change.getChangeDescription(), change, gwtChange);

			// Determine if have GWT Services
			if ((gwtServiceAsyncInterfaceNames != null)
					&& (gwtServiceAsyncInterfaceNames.length > 0)) {

				// Obtain the property value for the GWT Services
				StringBuilder propertyValue = new StringBuilder();
				boolean isFirst = true;
				for (String gstServiceAsyncInterfaceName : gwtServiceAsyncInterfaceNames) {
					if (!isFirst) {
						propertyValue.append(",");
					}
					isFirst = false;
					propertyValue.append(gstServiceAsyncInterfaceName);
				}

				// Add the property
				gwtExtension
						.addProperty(new PropertyModel(
								GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
								propertyValue.toString()));
			}
		}

		// Determine if extend with Comet functionality
		if (isEnableComet) {

			// Add the Comet Extension
			WoofTemplateExtensionModel cometExtension = new WoofTemplateExtensionModel(
					CometWoofTemplateExtensionService.EXTENSION_ALIAS);
			template.addExtension(cometExtension);

			// Determine if specify the manual publish method
			if (cometManualPublishMethodName != null) {
				cometExtension
						.addProperty(new PropertyModel(
								CometHttpTemplateSectionExtension.PROPERTY_MANUAL_PUBLISH_METHOD_NAME,
								cometManualPublishMethodName));
			}
		}

		// Return the change
		return change;
	}

	@Override
	public Change<WoofTemplateModel> changeTemplateUri(
			final WoofTemplateModel template, final String uri) {

		// Keep track of original values
		final String originalTemplateName = template.getWoofTemplateName();
		final String originalUri = template.getUri();

		// Obtain the template name after URI change
		final String newTemplateName = getTemplateName(
				template.getTemplatePath(), uri, template,
				this.model.getWoofTemplates());

		// Obtain GWT Extension and GWT Module path property
		PropertyModel gwtModuleProperty = null;
		WoofTemplateExtensionModel gwtExtension = null;
		for (WoofTemplateExtensionModel extension : template.getExtensions()) {
			if (GwtWoofTemplateExtensionService.EXTENSION_ALIAS
					.equals(extension.getExtensionClassName())) {
				gwtExtension = extension;
			}
		}
		if (gwtExtension != null) {
			// Obtain the GWT Module path
			for (PropertyModel property : gwtExtension.getProperties()) {
				if (PROPERTY_GWT_MODULE_PATH.equals(property.getName())) {
					gwtModuleProperty = property;
				}
			}
		}
		final String existingGwtModulePath = (gwtModuleProperty == null ? null
				: gwtModuleProperty.getValue());

		// Retrieve the new GWT Module path (keeping existing if not found)
		GwtModuleModel gwtModule = null;
		if (existingGwtModulePath != null) {
			gwtModule = this.gwtChanges
					.retrieveGwtModule(existingGwtModulePath);
			gwtModule.setRenameTo(uri);
		}
		final String newGwtModulePath = (gwtModule == null ? null
				: this.gwtChanges.createGwtModulePath(gwtModule));

		// Create change to template URI
		final PropertyModel finalGwtModuleProperty = gwtModuleProperty;
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(
				template, "Change Template URI") {
			@Override
			public void apply() {
				// Update template URI
				template.setUri(uri);
				template.setWoofTemplateName(newTemplateName);
				WoofChangesImpl.this.sortTemplates();

				// Update GWT (if able to update)
				if ((finalGwtModuleProperty != null)
						&& (newGwtModulePath != null)) {
					finalGwtModuleProperty.setValue(newGwtModulePath);
				}
			}

			@Override
			public void revert() {

				// Revert template URI
				template.setUri(originalUri);
				template.setWoofTemplateName(originalTemplateName);
				WoofChangesImpl.this.sortTemplates();

				// Revert GWT (if able to revert)
				if (finalGwtModuleProperty != null) {
					finalGwtModuleProperty.setValue(existingGwtModulePath);
				}
			}
		};

		// Include change to update GWT Module
		if (existingGwtModulePath != null) {
			Change<GwtModuleModel> gwtChange = this.gwtChanges.updateGwtModule(
					gwtModule, existingGwtModulePath);
			change = new AggregateChange<WoofTemplateModel>(change.getTarget(),
					change.getChangeDescription(), change, gwtChange);
		}

		// Return the change
		return change;
	}

	@Override
	public Change<WoofTemplateModel> removeTemplate(
			final WoofTemplateModel template) {

		// Ensure template available to remove
		boolean isInModel = false;
		for (WoofTemplateModel model : this.model.getWoofTemplates()) {
			if (model == template) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Template model not in model
			return new NoChange<WoofTemplateModel>(template, "Remove template "
					+ template.getWoofTemplateName(), "Template "
					+ template.getWoofTemplateName() + " is not in WoOF model");
		}

		// Return change to remove template
		return new AbstractChange<WoofTemplateModel>(template,
				"Remove template " + template.getWoofTemplateName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(template.getWoofTemplateOutputs(), list);
				removeConnections(template.getWoofSectionOutputs(), list);
				removeConnections(template.getWoofExceptions(), list);
				for (WoofTemplateOutputModel output : template.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofResource(), list);
				}
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the template
				WoofChangesImpl.this.model.removeWoofTemplate(template);
			}

			@Override
			public void revert() {
				// Add back the template
				WoofChangesImpl.this.model.addWoofTemplate(template);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortTemplates();
			}
		};
	}

	@Override
	public Change<WoofSectionModel> addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties, SectionType section,
			Map<String, String> inputToUri) {

		// Obtain the unique section name
		sectionName = getUniqueName(sectionName, null,
				this.model.getWoofSections(), SECTION_NAME_EXTRACTOR);

		// Create the section
		final WoofSectionModel woofSection = new WoofSectionModel(sectionName,
				sectionSourceClassName, sectionLocation);

		// Add the properties (if available)
		if (properties != null) {
			for (Property property : properties) {
				woofSection.addProperty(new PropertyModel(property.getName(),
						property.getValue()));
			}
		}

		// Add the inputs
		for (SectionInputType input : section.getSectionInputTypes()) {
			String inputName = input.getSectionInputName();
			String parameterType = input.getParameterType();
			String uri = inputToUri.get(inputName);
			woofSection.addInput(new WoofSectionInputModel(inputName,
					parameterType, uri));
		}

		// Add the outputs
		for (SectionOutputType output : section.getSectionOutputTypes()) {

			// Ignore escalations
			if (output.isEscalationOnly()) {
				continue;
			}

			// Add the output
			String outputName = output.getSectionOutputName();
			String argumentType = output.getArgumentType();
			woofSection.addOutput(new WoofSectionOutputModel(outputName,
					argumentType));
		}

		// Return the change to add section
		return new AbstractChange<WoofSectionModel>(woofSection, "Add Section") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofSection(woofSection);
				WoofChangesImpl.this.sortSections();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofSection(woofSection);
			}
		};
	}

	@Override
	public Change<WoofSectionInputModel> changeSectionInputUri(
			final WoofSectionInputModel sectionInput, final String uri) {

		// Maintain original URI
		final String originalUri = sectionInput.getUri();

		// Return change to URI
		return new AbstractChange<WoofSectionInputModel>(sectionInput,
				"Change Section Input URI") {
			@Override
			public void apply() {
				sectionInput.setUri(uri);
			}

			@Override
			public void revert() {
				sectionInput.setUri(originalUri);
			}
		};
	}

	@Override
	public Change<WoofSectionModel> removeSection(final WoofSectionModel section) {

		// Ensure section available to remove
		boolean isInModel = false;
		for (WoofSectionModel model : this.model.getWoofSections()) {
			if (model == section) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Section model not in model
			return new NoChange<WoofSectionModel>(section, "Remove section "
					+ section.getWoofSectionName(), "Section "
					+ section.getWoofSectionName() + " is not in WoOF model");
		}

		// Return change to remove section
		return new AbstractChange<WoofSectionModel>(section, "Remove section "
				+ section.getWoofSectionName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				for (WoofSectionInputModel input : section.getInputs()) {
					removeConnections(input.getWoofTemplateOutputs(), list);
					removeConnections(input.getWoofSectionOutputs(), list);
					removeConnections(input.getWoofExceptions(), list);
				}
				for (WoofSectionOutputModel output : section.getOutputs()) {
					removeConnection(output.getWoofTemplate(), list);
					removeConnection(output.getWoofSectionInput(), list);
					removeConnection(output.getWoofResource(), list);
				}
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the section
				WoofChangesImpl.this.model.removeWoofSection(section);
			}

			@Override
			public void revert() {
				// Add back the section
				WoofChangesImpl.this.model.addWoofSection(section);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortSections();
			}
		};
	}

	@Override
	public Change<WoofGovernanceModel> addGovernance(String governanceName,
			String governanceSourceClassName, PropertyList properties,
			GovernanceType<?, ?> governanceType) {

		// Obtain the unique governance name
		governanceName = getUniqueName(governanceName, null,
				this.model.getWoofGovernances(), GOVERNANCE_NAME_EXTRACTOR);

		// Create the governance
		final WoofGovernanceModel woofGovernance = new WoofGovernanceModel(
				governanceName, governanceSourceClassName);

		// Add the properties (if available)
		if (properties != null) {
			for (Property property : properties) {
				woofGovernance.addProperty(new PropertyModel(
						property.getName(), property.getValue()));
			}
		}

		// Return the change to add governance
		return new AbstractChange<WoofGovernanceModel>(woofGovernance,
				"Add Governance") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofGovernance(woofGovernance);
				WoofChangesImpl.this.sortGovernances();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofGovernance(woofGovernance);
			}
		};
	}

	@Override
	public Change<WoofGovernanceModel> removeGovernance(
			final WoofGovernanceModel governance) {

		// Ensure governance available to remove
		boolean isInModel = false;
		for (WoofGovernanceModel model : this.model.getWoofGovernances()) {
			if (model == governance) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Governance model not in model
			return new NoChange<WoofGovernanceModel>(governance,
					"Remove governance " + governance.getWoofGovernanceName(),
					"Governance " + governance.getWoofGovernanceName()
							+ " is not in WoOF model");
		}

		// Return change to remove governance
		return new AbstractChange<WoofGovernanceModel>(governance,
				"Remove governance " + governance.getWoofGovernanceName()) {

			@Override
			public void apply() {
				// Remove the governance
				WoofChangesImpl.this.model.removeWoofGovernance(governance);
			}

			@Override
			public void revert() {
				// Add back the governance
				WoofChangesImpl.this.model.addWoofGovernance(governance);
				WoofChangesImpl.this.sortGovernances();
			}
		};
	}

	@Override
	public Change<WoofGovernanceAreaModel> addGovernanceArea(
			final WoofGovernanceModel governance, int width, int height) {

		// Create the governance area
		final WoofGovernanceAreaModel area = new WoofGovernanceAreaModel(width,
				height);

		// Return the change to add governance area
		return new AbstractChange<WoofGovernanceAreaModel>(area,
				"Add governance area") {
			@Override
			public void apply() {
				governance.addGovernanceArea(area);
				WoofChangesImpl.this.sortGovernanceAreas(governance);
			}

			@Override
			public void revert() {
				governance.removeGovernanceArea(area);
			}
		};
	}

	@Override
	public Change<WoofGovernanceAreaModel> removeGovernanceArea(
			final WoofGovernanceAreaModel governanceArea) {

		// Ensure governance area in WoOF to remove
		final WoofGovernanceModel governance = this
				.getGovernance(governanceArea);
		if (governance == null) {
			// Governance area not in model
			return new NoChange<WoofGovernanceAreaModel>(governanceArea,
					"Remove governance area ",
					"Governance area is not in WoOF model");
		}

		// Return change to remove governance area
		return new AbstractChange<WoofGovernanceAreaModel>(governanceArea,
				"Remove governance area") {

			@Override
			public void apply() {
				// Remove the governance area
				governance.removeGovernanceArea(governanceArea);
			}

			@Override
			public void revert() {
				// Add back the governance area
				governance.addGovernanceArea(governanceArea);
				WoofChangesImpl.this.sortGovernanceAreas(governance);
			}
		};
	}

	@Override
	public Change<WoofResourceModel> addResource(String resourcePath) {

		// Obtain unique resource name
		String resourceName = getUniqueName(resourcePath, null,
				this.model.getWoofResources(), RESOURCE_NAME_EXTRACTOR);

		// Create the resource
		final WoofResourceModel resource = new WoofResourceModel(resourceName,
				resourcePath);

		// Return change to add resource
		return new AbstractChange<WoofResourceModel>(resource, "Add Resource") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofResource(resource);
				WoofChangesImpl.this.sortResources();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofResource(resource);
			}
		};
	}

	@Override
	public Change<WoofResourceModel> changeResourcePath(
			final WoofResourceModel resource, final String resourcePath) {

		// No change if no resource path
		if (CompileUtil.isBlank(resourcePath)) {
			return new NoChange<WoofResourceModel>(resource,
					"Change Resource Path", "Must provide resource path");
		}

		// Track original values
		final String originalName = resource.getWoofResourceName();
		final String originalPath = resource.getResourcePath();

		// Obtain the resource name after the resource path
		final String newName = getUniqueName(resourcePath, resource,
				this.model.getWoofResources(), RESOURCE_NAME_EXTRACTOR);

		// Return change to resource path
		return new AbstractChange<WoofResourceModel>(resource,
				"Change Resource Path") {
			@Override
			public void apply() {
				resource.setResourcePath(resourcePath);
				resource.setWoofResourceName(newName);
				WoofChangesImpl.this.sortResources();
			}

			@Override
			public void revert() {
				resource.setResourcePath(originalPath);
				resource.setWoofResourceName(originalName);
				WoofChangesImpl.this.sortResources();
			}
		};
	};

	@Override
	public Change<WoofResourceModel> removeResource(
			final WoofResourceModel resource) {

		// Ensure resource available to remove
		boolean isInModel = false;
		for (WoofResourceModel model : this.model.getWoofResources()) {
			if (model == resource) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Resource model not in model
			return new NoChange<WoofResourceModel>(resource, "Remove resource "
					+ resource.getWoofResourceName(), "Resource "
					+ resource.getWoofResourceName() + " is not in WoOF model");
		}

		// Return change to remove resource
		return new AbstractChange<WoofResourceModel>(resource,
				"Remove resource " + resource.getWoofResourceName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnections(resource.getWoofTemplateOutputs(), list);
				removeConnections(resource.getWoofSectionOutputs(), list);
				removeConnections(resource.getWoofExceptions(), list);
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the resource
				WoofChangesImpl.this.model.removeWoofResource(resource);
			}

			@Override
			public void revert() {
				// Add back the resource
				WoofChangesImpl.this.model.addWoofResource(resource);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortResources();
			}
		};
	}

	@Override
	public Change<WoofExceptionModel> addException(String exceptionClassName) {

		// Determine if the exception has already been added
		WoofExceptionModel existingModel = null;
		for (WoofExceptionModel model : this.model.getWoofExceptions()) {
			if (model.getClassName().equals(exceptionClassName)) {
				existingModel = model;
			}
		}
		if (existingModel != null) {
			// Provide change to only move model back on revert
			final WoofExceptionModel model = existingModel;
			final int x = existingModel.getX();
			final int y = existingModel.getY();
			return new AbstractChange<WoofExceptionModel>(model,
					"Add Exception") {
				@Override
				public void apply() {
					// No change as will be positioned
				}

				@Override
				public void revert() {
					// Move back to old position
					model.setX(x);
					model.setY(y);
				}
			};
		}

		// Create the exception
		final WoofExceptionModel exception = new WoofExceptionModel(
				exceptionClassName);

		// Return change to add exception
		return new AbstractChange<WoofExceptionModel>(exception,
				"Add Exception") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofException(exception);
				WoofChangesImpl.this.sortExceptions();
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofException(exception);
			}
		};
	}

	@Override
	public Change<WoofExceptionModel> removeException(
			final WoofExceptionModel exception) {

		// Ensure exception available to remove
		boolean isInModel = false;
		for (WoofExceptionModel model : this.model.getWoofExceptions()) {
			if (model == exception) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Exception model not in model
			return new NoChange<WoofExceptionModel>(exception,
					"Remove exception " + exception.getClassName(),
					"Exception " + exception.getClassName()
							+ " is not in WoOF model");
		}

		// Return change to remove exception
		return new AbstractChange<WoofExceptionModel>(exception,
				"Remove exception " + exception.getClassName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(exception.getWoofTemplate(), list);
				removeConnection(exception.getWoofSectionInput(), list);
				removeConnection(exception.getWoofResource(), list);
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the exception
				WoofChangesImpl.this.model.removeWoofException(exception);
			}

			@Override
			public void revert() {
				// Add back the exception
				WoofChangesImpl.this.model.addWoofException(exception);
				reconnectConnections(this.connections);
				WoofChangesImpl.this.sortExceptions();
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> linkTemplateOutputToTemplate(
			final WoofTemplateOutputModel templateOutput,
			WoofTemplateModel template) {

		// Create the connection
		final WoofTemplateOutputToWoofTemplateModel connection = new WoofTemplateOutputToWoofTemplateModel(
				template.getWoofTemplateName(), templateOutput, template);

		// Return change to link
		return new AddLinkChange<WoofTemplateOutputToWoofTemplateModel, WoofTemplateOutputModel>(
				connection, templateOutput, "Link Template Output to Template") {
			@Override
			protected void addExistingConnections(
					WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofTemplateModel> removeTemplateOuputToTemplate(
			final WoofTemplateOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofTemplateModel>(
				link, "Remove Template Output to Template");
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> linkTemplateOutputToSectionInput(
			final WoofTemplateOutputModel templateOutput,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofTemplateOutputToWoofSectionInputModel>(
					new WoofTemplateOutputToWoofSectionInputModel(),
					"The section input '"
							+ sectionInput.getWoofSectionInputName()
							+ "' was not found");
		}

		// Create the connection
		final WoofTemplateOutputToWoofSectionInputModel connection = new WoofTemplateOutputToWoofSectionInputModel(
				section.getWoofSectionName(),
				sectionInput.getWoofSectionInputName(), templateOutput,
				sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofTemplateOutputToWoofSectionInputModel, WoofTemplateOutputModel>(
				connection, templateOutput,
				"Link Template Output to Section Input") {
			@Override
			protected void addExistingConnections(
					WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofSectionInputModel> removeTemplateOuputToSectionInput(
			WoofTemplateOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofSectionInputModel>(
				link, "Remove Template Output to Section Input");
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> linkTemplateOutputToResource(
			WoofTemplateOutputModel templateOutput, WoofResourceModel resource) {

		// Create the connection
		final WoofTemplateOutputToWoofResourceModel connection = new WoofTemplateOutputToWoofResourceModel(
				resource.getWoofResourceName(), templateOutput, resource);

		// Return change to add connection
		return new AddLinkChange<WoofTemplateOutputToWoofResourceModel, WoofTemplateOutputModel>(
				connection, templateOutput, "Link Template Output to Resource") {
			@Override
			protected void addExistingConnections(
					WoofTemplateOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofTemplateOutputToWoofResourceModel> removeTemplateOuputToResource(
			WoofTemplateOutputToWoofResourceModel link) {
		return new RemoveLinkChange<WoofTemplateOutputToWoofResourceModel>(
				link, "Remove Template Output to Resource");
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> linkSectionOutputToTemplate(
			WoofSectionOutputModel sectionOutput, WoofTemplateModel template) {

		// Create the connection
		final WoofSectionOutputToWoofTemplateModel connection = new WoofSectionOutputToWoofTemplateModel(
				template.getWoofTemplateName(), sectionOutput, template);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofTemplateModel, WoofSectionOutputModel>(
				connection, sectionOutput, "Link Section Output to Template") {
			@Override
			protected void addExistingConnections(
					WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofTemplateModel> removeSectionOuputToTemplate(
			WoofSectionOutputToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofTemplateModel>(link,
				"Remove Section Output to Template");
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> linkSectionOutputToSectionInput(
			WoofSectionOutputModel sectionOutput,
			WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofSectionOutputToWoofSectionInputModel>(
					new WoofSectionOutputToWoofSectionInputModel(),
					"The section input '"
							+ sectionInput.getWoofSectionInputName()
							+ "' was not found");
		}

		// Create the connection
		final WoofSectionOutputToWoofSectionInputModel connection = new WoofSectionOutputToWoofSectionInputModel(
				section.getWoofSectionName(),
				sectionInput.getWoofSectionInputName(), sectionOutput,
				sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofSectionInputModel, WoofSectionOutputModel>(
				connection, sectionOutput,
				"Link Section Output to Section Input") {
			@Override
			protected void addExistingConnections(
					WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofSectionInputModel> removeSectionOuputToSectionInput(
			WoofSectionOutputToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofSectionInputModel>(
				link, "Remove Section Output to Section Input");
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> linkSectionOutputToResource(
			WoofSectionOutputModel sectionOutput, WoofResourceModel resource) {

		// Create the connection
		final WoofSectionOutputToWoofResourceModel connection = new WoofSectionOutputToWoofResourceModel(
				resource.getWoofResourceName(), sectionOutput, resource);

		// Return change to add connection
		return new AddLinkChange<WoofSectionOutputToWoofResourceModel, WoofSectionOutputModel>(
				connection, sectionOutput, "Link Section Output to Resource") {
			@Override
			protected void addExistingConnections(
					WoofSectionOutputModel source, List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofSectionOutputToWoofResourceModel> removeSectionOuputToResource(
			WoofSectionOutputToWoofResourceModel link) {
		return new RemoveLinkChange<WoofSectionOutputToWoofResourceModel>(link,
				"Remove Section Output to Resource");
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> linkExceptionToTemplate(
			WoofExceptionModel exception, WoofTemplateModel template) {

		// Create the connection
		final WoofExceptionToWoofTemplateModel connection = new WoofExceptionToWoofTemplateModel(
				template.getWoofTemplateName(), exception, template);

		// Return change to add connection
		return new AddLinkChange<WoofExceptionToWoofTemplateModel, WoofExceptionModel>(
				connection, exception, "Link Exception to Template") {
			@Override
			protected void addExistingConnections(WoofExceptionModel source,
					List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofExceptionToWoofTemplateModel> removeExceptionToTemplate(
			WoofExceptionToWoofTemplateModel link) {
		return new RemoveLinkChange<WoofExceptionToWoofTemplateModel>(link,
				"Remove Exception to Template");
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> linkExceptionToSectionInput(
			WoofExceptionModel exception, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofExceptionToWoofSectionInputModel>(
					new WoofExceptionToWoofSectionInputModel(),
					"The section input '"
							+ sectionInput.getWoofSectionInputName()
							+ "' was not found");
		}

		// Create the connection
		final WoofExceptionToWoofSectionInputModel connection = new WoofExceptionToWoofSectionInputModel(
				section.getWoofSectionName(),
				sectionInput.getWoofSectionInputName(), exception, sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofExceptionToWoofSectionInputModel, WoofExceptionModel>(
				connection, exception, "Link Exception to Section Input") {
			@Override
			protected void addExistingConnections(WoofExceptionModel source,
					List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofExceptionToWoofSectionInputModel> removeExceptionToSectionInput(
			WoofExceptionToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofExceptionToWoofSectionInputModel>(link,
				"Remove Exception to Section Input");
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> linkExceptionToResource(
			WoofExceptionModel exception, WoofResourceModel resource) {

		// Create the connection
		final WoofExceptionToWoofResourceModel connection = new WoofExceptionToWoofResourceModel(
				resource.getWoofResourceName(), exception, resource);

		// Return change to add connection
		return new AddLinkChange<WoofExceptionToWoofResourceModel, WoofExceptionModel>(
				connection, exception, "Link Exception to Resource") {
			@Override
			protected void addExistingConnections(WoofExceptionModel source,
					List<ConnectionModel> list) {
				list.add(source.getWoofTemplate());
				list.add(source.getWoofSectionInput());
				list.add(source.getWoofResource());
			}
		};
	}

	@Override
	public Change<WoofExceptionToWoofResourceModel> removeExceptionToResource(
			WoofExceptionToWoofResourceModel link) {
		return new RemoveLinkChange<WoofExceptionToWoofResourceModel>(link,
				"Remove Exception to Resource");
	}

	/**
	 * Abstract {@link Change} to add a {@link ConnectionModel}.
	 */
	private abstract class AddLinkChange<C extends ConnectionModel, S extends Model>
			extends AbstractChange<C> {

		/**
		 * Source for {@link ConnectionModel}.
		 */
		private final S source;

		/**
		 * {@link ConnectionModel} instances.
		 */
		private ConnectionModel[] connections;

		/**
		 * Initiate.
		 * 
		 * @param connection
		 *            {@link ConnectionModel}.
		 * @param source
		 *            Source for {@link ConnectionModel}.
		 * @param changeDescription
		 *            Change descriptions.
		 */
		public AddLinkChange(C connection, S source, String changeDescription) {
			super(connection, changeDescription);
			this.source = source;
		}

		/**
		 * Adds the existing {@link ConnectionModel} instances.
		 * 
		 * @param source
		 *            Source of the {@link ConnectionModel}.
		 * @param list
		 *            List to add the {@link ConnectionModel} instances.
		 */
		protected abstract void addExistingConnections(S source,
				List<ConnectionModel> list);

		/*
		 * ====================== Change ======================
		 */

		@Override
		public void apply() {

			// Obtain existing connections
			List<ConnectionModel> existingLinks = new LinkedList<ConnectionModel>();
			this.addExistingConnections(this.source, existingLinks);

			// Remove the existing connections
			List<ConnectionModel> list = new LinkedList<ConnectionModel>();
			for (ConnectionModel existingLink : existingLinks) {
				removeConnection(existingLink, list);
			}
			this.connections = list.toArray(new ConnectionModel[list.size()]);

			// Connect
			this.getTarget().connect();
		}

		@Override
		public void revert() {

			// Remove the connection
			this.getTarget().remove();

			// Reconnect previous connections
			reconnectConnections(this.connections);
		}
	};

	/**
	 * {@link Change} to remove the {@link ConnectionModel}.
	 */
	private class RemoveLinkChange<C extends ConnectionModel> extends
			AbstractChange<C> {

		/**
		 * Initiate.
		 * 
		 * @param connection
		 *            {@link ConnectionModel}.
		 * @param changeDescription
		 *            Change description.
		 */
		public RemoveLinkChange(C connection, String changeDescription) {
			super(connection, changeDescription);
		}

		/*
		 * ==================== Change =======================
		 */

		@Override
		public void apply() {
			// Remove connection
			this.getTarget().remove();
		}

		@Override
		public void revert() {
			// Reconnect
			this.getTarget().connect();
		}
	}

}