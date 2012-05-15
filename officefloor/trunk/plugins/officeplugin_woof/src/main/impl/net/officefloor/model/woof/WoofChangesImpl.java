/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.util.HashMap;
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
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;
import net.officefloor.plugin.woof.comet.CometWoofTemplateExtensionService;
import net.officefloor.plugin.woof.gwt.GwtWoofTemplateExtensionService;

/**
 * {@link Change} for the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofChangesImpl implements WoofChanges {

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
	 * {@link WoofTemplateExtensionModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofTemplateExtensionModel> TEMPLATE_EXTENSION_NAME_EXTRACTOR = new NameExtractor<WoofTemplateExtensionModel>() {
		@Override
		public String extractName(WoofTemplateExtensionModel model) {
			return model.getExtensionClassName();
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
	 * {@link WoofSectionInputModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofSectionInputModel> SECTION_INPUT_NAME_EXTRACTOR = new NameExtractor<WoofSectionInputModel>() {
		@Override
		public String extractName(WoofSectionInputModel model) {
			return model.getWoofSectionInputName();
		}
	};

	/**
	 * {@link WoofSectionOutputModel} {@link NameExtractor}.
	 */
	private static final NameExtractor<WoofSectionOutputModel> SECTION_OUTPUT_NAME_EXTRACTOR = new NameExtractor<WoofSectionOutputModel>() {
		@Override
		public String extractName(WoofSectionOutputModel model) {
			return model.getWoofSectionOutputName();
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
	 * Obtains the {@link #PROPERTY_GWT_MODULE_PATH} {@link PropertyModel}.
	 * 
	 * @param extension
	 *            GWT {@link WoofTemplateExtensionModel}.
	 * @return {@link PropertyModel} or <code>null</code>.
	 */
	private static PropertyModel getGwtModulePathProperty(
			WoofTemplateExtensionModel gwtExtension) {
		return getTemplateExtensionProperty(gwtExtension,
				PROPERTY_GWT_MODULE_PATH);
	}

	/**
	 * Obtains the GWT Async Interfaces {@link PropertyModel}.
	 * 
	 * @param gwtExtension
	 *            GWT {@link WoofTemplateExtensionModel}.
	 * @return {@link PropertyModel} or <code>null</code>.
	 */
	private static PropertyModel getGwtAsyncInterfacesProperty(
			WoofTemplateExtensionModel gwtExtension) {
		return getTemplateExtensionProperty(
				gwtExtension,
				GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES);
	}

	/**
	 * Obtains the Comet manual publish method {@link PropertyModel}.
	 * 
	 * @param cometExtension
	 *            Comet {@link WoofTemplateExtensionModel}.
	 * @return {@link PropertyModel} or <code>null</code>.
	 */
	private static PropertyModel getCometManualPublishMethodProperty(
			WoofTemplateExtensionModel cometExtension) {
		return getTemplateExtensionProperty(
				cometExtension,
				CometHttpTemplateSectionExtension.PROPERTY_MANUAL_PUBLISH_METHOD_NAME);
	}

	/**
	 * Obtains the value for the {@link PropertyModel}.
	 * 
	 * @param property
	 *            {@link PropertyModel}. May be <code>null</code>.
	 * @return {@link PropertyModel} value or <code>null</code>.
	 */
	private static String getPropertyValue(PropertyModel property) {
		return (property == null ? null : property.getValue());
	}

	/**
	 * Obtains the {@link PropertyModel}.
	 * 
	 * @param extension
	 *            {@link WoofTemplateExtensionModel}.
	 * @param propertyName
	 *            Name of {@link PropertyModel}.
	 * @return {@link PropertyModel} or <code>null</code>.
	 */
	private static PropertyModel getTemplateExtensionProperty(
			WoofTemplateExtensionModel extension, String propertyName) {

		// Must have extension
		if (extension == null) {
			return null;
		}

		// Obtain the extension property
		for (PropertyModel property : extension.getProperties()) {
			if (propertyName.equals(property.getName())) {
				return property;
			}
		}

		// As here, no property
		return null;
	}

	/**
	 * Obtains the GWT template extension.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return GWT {@link WoofTemplateExtensionModel} or <code>null</code>.
	 */
	private static WoofTemplateExtensionModel getGwtTemplateExtension(
			WoofTemplateModel template) {
		return getTemplateExtension(template,
				GwtWoofTemplateExtensionService.EXTENSION_ALIAS);
	}

	/**
	 * Obtains the Comet template extension.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return Comet {@link WoofTemplateExtensionModel} or <code>null</code>.
	 */
	private static WoofTemplateExtensionModel getCometTemplateExtension(
			WoofTemplateModel template) {
		return getTemplateExtension(template,
				CometWoofTemplateExtensionService.EXTENSION_ALIAS);
	}

	/**
	 * Obtains the {@link WoofTemplateExtensionModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @param extensionClassName
	 *            {@link WoofTemplateExtensionModel} class name.
	 * @return {@link WoofTemplateExtensionModel} or <code>null</code>.
	 */
	private static WoofTemplateExtensionModel getTemplateExtension(
			WoofTemplateModel template, String extensionClassName) {

		// Search extension for appropriate extension
		for (WoofTemplateExtensionModel extension : template.getExtensions()) {
			if (extensionClassName.equals(extension.getExtensionClassName())) {
				return extension;
			}
		}

		// As here, no extension
		return null;
	}

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
	 * Sorts the {@link WoofTemplateOutputModel} and
	 * {@link WoofTemplateExtensionModel} instances of the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 */
	private static void sortTemplateOutputsExtensions(WoofTemplateModel template) {
		// Sort outputs keeping template render complete output last
		Collections.sort(template.getOutputs(),
				new Comparator<WoofTemplateOutputModel>() {
					@Override
					public int compare(WoofTemplateOutputModel a,
							WoofTemplateOutputModel b) {
						String nameA = a.getWoofTemplateOutputName();
						String nameB = b.getWoofTemplateOutputName();
						if (nameA.equals(nameB)) {
							return 0; // same
						} else if (HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME
								.equals(nameA)) {
							return 1; // render complete output always last
						} else if (HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME
								.equals(nameB)) {
							return -1; // render complete output always last
						} else {
							// Sort by name
							return String.CASE_INSENSITIVE_ORDER.compare(nameA,
									nameB);
						}
					}
				});

		// Sort the extensions
		sortByName(template.getExtensions(), TEMPLATE_EXTENSION_NAME_EXTRACTOR);
	}

	/**
	 * Sorts the {@link WoofSectionInputModel} and
	 * {@link WoofSectionOutputModel} instances of the {@link WoofSectionModel}.
	 * 
	 * @param section
	 *            {@link WoofSectionModel}.
	 */
	private static void sortSectionInputOutputs(WoofSectionModel section) {
		sortByName(section.getInputs(), SECTION_INPUT_NAME_EXTRACTOR);
		sortByName(section.getOutputs(), SECTION_OUTPUT_NAME_EXTRACTOR);
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
	 * Obtains the property value for GWT Asynchronous Services.
	 * 
	 * @param gwtServiceAsyncInterfaceNames
	 *            Listing of GWT Asynchronous Service Interface names.
	 * @return Property value for GWT Asynchronous Services.
	 */
	private static String getGwtAsyncServicesPropertyValue(
			String[] gwtServiceAsyncInterfaceNames) {

		// Obtain the property value for the GWT Services
		StringBuilder propertyValue = new StringBuilder();
		boolean isFirst = true;
		if (gwtServiceAsyncInterfaceNames != null) {
			for (String gstServiceAsyncInterfaceName : gwtServiceAsyncInterfaceNames) {
				if (!isFirst) {
					propertyValue.append(",");
				}
				isFirst = false;
				propertyValue.append(gstServiceAsyncInterfaceName);
			}
		}

		// Return the property value
		return propertyValue.toString();
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
	public String getGwtEntryPointClassName(WoofTemplateModel template) {

		// Obtain the GWT module path
		WoofTemplateExtensionModel gwtExtension = getGwtTemplateExtension(template);
		PropertyModel gwtModulePathProperty = getGwtModulePathProperty(gwtExtension);
		String gwtModulePath = getPropertyValue(gwtModulePathProperty);
		if (gwtModulePath != null) {

			// Obtain the GWT module
			GwtModuleModel gwtModule = this.gwtChanges
					.retrieveGwtModule(gwtModulePath);
			if (gwtModule != null) {

				// Return the EntryPoint class name
				return gwtModule.getEntryPointClassName();
			}
		}

		// As here, no GWT EntryPoint class name
		return null;
	}

	@Override
	public String[] getGwtAsyncServiceInterfaceNames(WoofTemplateModel template) {
		WoofTemplateExtensionModel gwtExtension = getGwtTemplateExtension(template);
		PropertyModel gwtAsyncInterfacesProperty = getGwtAsyncInterfacesProperty(gwtExtension);
		String gwtAsyncInterfaceValue = getPropertyValue(gwtAsyncInterfacesProperty);
		String[] gwtAsyncServiceInterfaceNames = GwtHttpTemplateSectionExtension
				.getGwtAsyncServiceInterfaceNames(gwtAsyncInterfaceValue);
		return gwtAsyncServiceInterfaceNames;
	}

	@Override
	public boolean isCometEnabled(WoofTemplateModel template) {
		WoofTemplateExtensionModel cometExtension = getCometTemplateExtension(template);
		return (cometExtension != null);
	}

	@Override
	public String getCometManualPublishMethodName(WoofTemplateModel template) {
		WoofTemplateExtensionModel cometExtension = getCometTemplateExtension(template);
		PropertyModel cometManualPublishMethodProperty = getCometManualPublishMethodProperty(cometExtension);
		return getPropertyValue(cometManualPublishMethodProperty);
	}

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
			gwtExtension.addProperty(new PropertyModel(
					PROPERTY_GWT_MODULE_PATH, gwtModulePath));

			// Include change for adding GWT Module
			Change<?> gwtChange = this.gwtChanges.updateGwtModule(module, null);

			// Create aggregate change to include GWT changes
			change = new AggregateChange<WoofTemplateModel>(change.getTarget(),
					change.getChangeDescription(), change, gwtChange);

			// Determine if have GWT Services
			if ((gwtServiceAsyncInterfaceNames != null)
					&& (gwtServiceAsyncInterfaceNames.length > 0)) {

				// Obtain the property value for the GWT Services
				String propertyValue = getGwtAsyncServicesPropertyValue(gwtServiceAsyncInterfaceNames);

				// Add the property
				gwtExtension
						.addProperty(new PropertyModel(
								GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
								propertyValue));
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

		// Sort outputs and extensions
		sortTemplateOutputsExtensions(template);

		// Return the change
		return change;
	}

	@Override
	public Change<WoofTemplateModel> refactorTemplate(
			final WoofTemplateModel template, final String templatePath,
			final String templateLogicClass, SectionType sectionType,
			final String uri, String gwtEntryPointClassName,
			String[] gwtServiceAsyncInterfaceNames, boolean isEnableComet,
			final String cometManualPublishMethodName,
			Map<String, String> templateOutputNameMapping) {

		// Obtain the template name after URI change
		final String newTemplateName = getTemplateName(templatePath, uri,
				template, this.model.getWoofTemplates());

		// Create change to sort outputs
		Change<WoofTemplateModel> sortChange = new AbstractChange<WoofTemplateModel>(
				template, "Sort outputs") {
			@Override
			public void apply() {
				sortTemplateOutputsExtensions(template);
			}

			@Override
			public void revert() {
				this.apply(); // sort
			}
		};

		// Provide list of changes to aggregate
		List<Change<?>> changes = new LinkedList<Change<?>>();

		// Sort outputs at start (so revert has right order)
		changes.add(sortChange);

		// Obtain the existing details
		final String existingTemplateName = template.getWoofTemplateName();
		final String existingTemplatePath = template.getTemplatePath();
		final String existingTemplateClassName = template
				.getTemplateClassName();
		final String existingUri = template.getUri();

		// Create change to attributes
		Change<WoofTemplateModel> attributeChange = new AbstractChange<WoofTemplateModel>(
				template, "Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				template.setWoofTemplateName(newTemplateName);
				template.setTemplatePath(templatePath);
				template.setTemplateClassName(templateLogicClass);
				template.setUri(uri);
			}

			@Override
			public void revert() {
				// Revert attributes
				template.setWoofTemplateName(existingTemplateName);
				template.setTemplatePath(existingTemplatePath);
				template.setTemplateClassName(existingTemplateClassName);
				template.setUri(existingUri);
			}
		};
		changes.add(attributeChange);

		// Obtain the GWT extension (and GWT Module Path)
		WoofTemplateExtensionModel gwtExtension = getGwtTemplateExtension(template);
		PropertyModel gwtModulePathProperty = getGwtModulePathProperty(gwtExtension);
		final String existingGwtModulePath = getPropertyValue(gwtModulePathProperty);
		PropertyModel gwtAsyncInterfacesProperty = getGwtAsyncInterfacesProperty(gwtExtension);
		final String existingGwtAsyncInterfaces = getPropertyValue(gwtAsyncInterfacesProperty);

		// Create the potential GWT Module and new property values
		GwtModuleModel gwtModule = new GwtModuleModel(uri,
				gwtEntryPointClassName, null);
		if (isEnableComet) {
			// Extend potential GWT Module for Comet
			CometHttpTemplateSectionExtension.extendGwtModule(gwtModule);
		}
		final String newGwtModulePath = this.gwtChanges
				.createGwtModulePath(gwtModule);
		final String newGwtAsyncInterfaces = getGwtAsyncServicesPropertyValue(gwtServiceAsyncInterfaceNames);

		// Provide details for GWT Module refactoring
		boolean isUpdateGwtModule = false;
		final WoofTemplateExtensionModel finalGwtExtension = gwtExtension;
		final boolean hasExistingGwtModulePathProperty;
		final PropertyModel finalGwtModulePathProperty;
		if (gwtModulePathProperty == null) {
			// No existing GWT Module Path property
			hasExistingGwtModulePathProperty = false;
			finalGwtModulePathProperty = new PropertyModel(
					PROPERTY_GWT_MODULE_PATH, newGwtModulePath);
		} else {
			// Has existing GWT Module Path property
			hasExistingGwtModulePathProperty = true;
			finalGwtModulePathProperty = gwtModulePathProperty;
		}
		final boolean hasExistingGwtAsyncServicesProperty;
		final PropertyModel finalGwtAsyncServicesProperty;
		if (gwtAsyncInterfacesProperty == null) {
			// No existing GWT Async Services property
			hasExistingGwtAsyncServicesProperty = false;
			finalGwtAsyncServicesProperty = new PropertyModel(
					GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
					newGwtAsyncInterfaces);
		} else {
			// Has existing GWT Async Services property
			hasExistingGwtAsyncServicesProperty = true;
			finalGwtAsyncServicesProperty = gwtAsyncInterfacesProperty;
		}

		// Refactor the GWT extension (either refactoring, adding or removing)
		boolean isExistingEntryPointClass = (gwtExtension != null);
		boolean isRefactoringToHaveEntryPointClass = (gwtEntryPointClassName != null)
				&& (gwtEntryPointClassName.trim().length() > 0);
		if (isExistingEntryPointClass) {
			// Existing to refactor or remove
			if (isRefactoringToHaveEntryPointClass) {
				// Refactor the GWT extension (and set to update GWT Module)
				isUpdateGwtModule = true;
				Change<WoofTemplateExtensionModel> change = new AbstractChange<WoofTemplateExtensionModel>(
						finalGwtExtension, "Refactor GWT Extension") {
					@Override
					public void apply() {

						// Refactor GWT Module Path
						finalGwtModulePathProperty.setValue(newGwtModulePath);
						if (!hasExistingGwtModulePathProperty) {
							finalGwtExtension
									.addProperty(finalGwtModulePathProperty);
						}

						// Refactor GWT Async Services
						finalGwtAsyncServicesProperty
								.setValue(newGwtAsyncInterfaces);
						if (!hasExistingGwtAsyncServicesProperty) {
							finalGwtExtension
									.addProperty(finalGwtAsyncServicesProperty);
						}
					}

					@Override
					public void revert() {

						// Refactor GWT Module Path
						finalGwtModulePathProperty
								.setValue(existingGwtModulePath);
						if (!hasExistingGwtModulePathProperty) {
							finalGwtExtension
									.removeProperty(finalGwtModulePathProperty);
						}

						// Refactor GWT Async Services
						finalGwtAsyncServicesProperty
								.setValue(existingGwtAsyncInterfaces);
						if (!hasExistingGwtAsyncServicesProperty) {
							finalGwtExtension
									.removeProperty(finalGwtAsyncServicesProperty);
						}
					}
				};
				changes.add(change);

			} else {
				// Remove the GWT extension
				Change<WoofTemplateExtensionModel> change = new AbstractChange<WoofTemplateExtensionModel>(
						finalGwtExtension, "Remove GWT Extension") {
					@Override
					public void apply() {
						template.removeExtension(finalGwtExtension);
					}

					@Override
					public void revert() {
						template.addExtension(finalGwtExtension);
					}
				};
				changes.add(change);
			}

		} else if (isRefactoringToHaveEntryPointClass) {
			// Add the GWT extension (and set to update GWT Module)
			isUpdateGwtModule = true;
			final WoofTemplateExtensionModel addGwtExtension = new WoofTemplateExtensionModel(
					GwtWoofTemplateExtensionService.EXTENSION_ALIAS);
			addGwtExtension.addProperty(finalGwtModulePathProperty);
			addGwtExtension.addProperty(finalGwtAsyncServicesProperty);
			Change<WoofTemplateExtensionModel> change = new AbstractChange<WoofTemplateExtensionModel>(
					addGwtExtension, "Add GWT Extension") {
				@Override
				public void apply() {
					template.addExtension(addGwtExtension);
				}

				@Override
				public void revert() {
					template.removeExtension(addGwtExtension);
				}
			};
			changes.add(change);
		}

		// Add change to refactor the GWT Module (if required)
		if (isUpdateGwtModule) {
			changes.add(this.gwtChanges.updateGwtModule(gwtModule,
					existingGwtModulePath));
		}

		// Obtain the Comet extension (and manual publish property)
		final WoofTemplateExtensionModel cometExtension = getCometTemplateExtension(template);
		PropertyModel cometManualPublishProperty = getCometManualPublishMethodProperty(cometExtension);
		final String existingCometManualPublishMethodName = getPropertyValue(cometManualPublishProperty);

		// Provide details for Comet refactoring
		final boolean hasExistingCometManualPathPathProperty;
		final PropertyModel finalCometManualPathProperty;
		if (cometManualPublishProperty == null) {
			// No existing Comet manual publish property
			hasExistingCometManualPathPathProperty = false;
			finalCometManualPathProperty = new PropertyModel(
					CometHttpTemplateSectionExtension.PROPERTY_MANUAL_PUBLISH_METHOD_NAME,
					cometManualPublishMethodName);
		} else {
			// Has existing Comet manual publish property
			hasExistingCometManualPathPathProperty = true;
			finalCometManualPathProperty = cometManualPublishProperty;
		}

		// Refactor the Comet extension (either refactoring, adding or removing)
		boolean isExistingCometExtension = (cometExtension != null);
		if (isExistingCometExtension) {
			// Existing to refactor or remove
			if (isEnableComet) {
				// Refactor Comet extension
				Change<WoofTemplateExtensionModel> change = new AbstractChange<WoofTemplateExtensionModel>(
						cometExtension, "Refactor Comet Extension") {
					@Override
					public void apply() {
						// Refactor manual publish
						finalCometManualPathProperty
								.setValue(cometManualPublishMethodName);
						if (!hasExistingCometManualPathPathProperty) {
							cometExtension
									.addProperty(finalCometManualPathProperty);
						}
					}

					@Override
					public void revert() {
						// Revert manual publish
						finalCometManualPathProperty
								.setValue(existingCometManualPublishMethodName);
						if (!hasExistingCometManualPathPathProperty) {
							cometExtension
									.removeProperty(finalCometManualPathProperty);
						}
					}
				};
				changes.add(change);

			} else {
				// Remove Comet extension
				Change<WoofTemplateExtensionModel> change = new AbstractChange<WoofTemplateExtensionModel>(
						cometExtension, "Remove Comet Extension") {
					@Override
					public void apply() {
						template.removeExtension(cometExtension);
					}

					@Override
					public void revert() {
						template.addExtension(cometExtension);
					}
				};
				changes.add(change);
			}

		} else if (isEnableComet) {
			// Create Comet extension to add
			final WoofTemplateExtensionModel addCometExtension = new WoofTemplateExtensionModel(
					CometWoofTemplateExtensionService.EXTENSION_ALIAS);
			if ((cometManualPublishMethodName != null)
					&& (cometManualPublishMethodName.trim().length() > 0)) {
				// Add manual publish method name
				addCometExtension.addProperty(finalCometManualPathProperty);
			}

			// Add Comet extension
			Change<WoofTemplateExtensionModel> change = new AbstractChange<WoofTemplateExtensionModel>(
					cometExtension, "Add Comet Extension") {
				@Override
				public void apply() {
					template.addExtension(addCometExtension);
				}

				@Override
				public void revert() {
					template.removeExtension(addCometExtension);
				}
			};
			changes.add(change);
		}

		// Obtain the mapping of existing outputs
		Map<String, WoofTemplateOutputModel> existingOutputNameMapping = new HashMap<String, WoofTemplateOutputModel>();
		for (WoofTemplateOutputModel output : template.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofTemplateOutputName(),
					output);
		}

		// Refactor the outputs (either refactoring, adding or removing)
		for (final SectionOutputType outputType : sectionType
				.getSectionOutputTypes()) {

			// Ignore escalations
			if (outputType.isEscalationOnly()) {
				continue;
			}

			// Obtain the mapped section output model
			final String outputName = outputType.getSectionOutputName();
			String mappedOutputName = templateOutputNameMapping.get(outputName);
			final WoofTemplateOutputModel existingOutputModel = existingOutputNameMapping
					.remove(mappedOutputName);

			// Obtain further type details
			final String argumentType = outputType.getArgumentType();

			// Determine action to take based on existing output
			Change<WoofTemplateOutputModel> templateOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel
						.getWoofTemplateOutputName();
				final String existingArgumentType = existingOutputModel
						.getArgumentType();
				templateOutputChange = new AbstractChange<WoofTemplateOutputModel>(
						existingOutputModel, "Refactor Template Output") {
					@Override
					public void apply() {
						existingOutputModel
								.setWoofTemplateOutputName(outputName);
						existingOutputModel.setArgumentType(argumentType);
					}

					@Override
					public void revert() {
						existingOutputModel
								.setWoofTemplateOutputName(existingOutputName);
						existingOutputModel
								.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output (with no URI)
				final WoofTemplateOutputModel newOutputModel = new WoofTemplateOutputModel(
						outputName, argumentType);
				templateOutputChange = new AbstractChange<WoofTemplateOutputModel>(
						newOutputModel, "Add Template Output") {
					@Override
					public void apply() {
						template.addOutput(newOutputModel);
					}

					@Override
					public void revert() {
						template.removeOutput(newOutputModel);
					}
				};
			}
			changes.add(templateOutputChange);
		}
		for (final WoofTemplateOutputModel unmappedOutputModel : existingOutputNameMapping
				.values()) {
			// Create change to remove the unmapped output model
			Change<WoofTemplateOutputModel> unmappedOutputChange = new AbstractChange<WoofTemplateOutputModel>(
					unmappedOutputModel, "Remove Template Output") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnection(unmappedOutputModel.getWoofResource(),
							list);
					removeConnection(unmappedOutputModel.getWoofSectionInput(),
							list);
					removeConnection(unmappedOutputModel.getWoofTemplate(),
							list);
					this.connections = list.toArray(new ConnectionModel[list
							.size()]);

					// Remove the template output
					template.removeOutput(unmappedOutputModel);
				}

				@Override
				public void revert() {

					// Add output back to template
					template.addOutput(unmappedOutputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedOutputChange);
		}

		// Sort outputs at end (so apply has right order)
		changes.add(sortChange);

		// Return aggregate change for refactoring
		return new AggregateChange<WoofTemplateModel>(template,
				"Refactor Template",
				changes.toArray(new Change[changes.size()]));
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
		WoofTemplateExtensionModel gwtExtension = getGwtTemplateExtension(template);
		final PropertyModel gwtModuleProperty = getGwtModulePathProperty(gwtExtension);
		final String existingGwtModulePath = getPropertyValue(gwtModuleProperty);

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
		Change<WoofTemplateModel> change = new AbstractChange<WoofTemplateModel>(
				template, "Change Template URI") {
			@Override
			public void apply() {
				// Update template URI
				template.setUri(uri);
				template.setWoofTemplateName(newTemplateName);
				WoofChangesImpl.this.sortTemplates();

				// Update GWT (if able to update)
				if ((gwtModuleProperty != null) && (newGwtModulePath != null)) {
					gwtModuleProperty.setValue(newGwtModulePath);
				}
			}

			@Override
			public void revert() {

				// Revert template URI
				template.setUri(originalUri);
				template.setWoofTemplateName(originalTemplateName);
				WoofChangesImpl.this.sortTemplates();

				// Revert GWT (if able to revert)
				if (gwtModuleProperty != null) {
					gwtModuleProperty.setValue(existingGwtModulePath);
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

		// Sort the inputs/outputs
		sortSectionInputOutputs(woofSection);

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
	public Change<WoofSectionModel> refactorSection(
			final WoofSectionModel section, final String sectionName,
			final String sectionSourceClassName, final String sectionLocation,
			final PropertyList properties, final SectionType sectionType,
			Map<String, String> sectionInputNameMapping,
			Map<String, String> sectionOutputNameMapping) {

		// Ensure section available to remove
		boolean isInModel = false;
		for (WoofSectionModel model : this.model.getWoofSections()) {
			if (model == section) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Section model not in model
			return new NoChange<WoofSectionModel>(section, "Refactor section",
					"Section " + section.getWoofSectionName()
							+ " is not in WoOF model");
		}

		// Create change to sort inputs/outputs
		Change<WoofSectionModel> sortChange = new AbstractChange<WoofSectionModel>(
				section, "Sort inputs/outputs") {
			@Override
			public void apply() {
				sortSectionInputOutputs(section);
			}

			@Override
			public void revert() {
				this.apply(); // sort
			}
		};

		// Provide list of changes to aggregate
		List<Change<?>> changes = new LinkedList<Change<?>>();

		// Sort inputs/outputs at start (so revert has right order)
		changes.add(sortChange);

		// Obtain the existing details
		final String existingSectionName = section.getWoofSectionName();
		final String existingSectionSourceClassName = section
				.getSectionSourceClassName();
		final String existingSectionLocation = section.getSectionLocation();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(
				section.getProperties());

		// Create change to attributes and properties
		Change<WoofSectionModel> attributeChange = new AbstractChange<WoofSectionModel>(
				section, "Refactor attributes") {
			@Override
			public void apply() {
				// Refactor details
				section.setWoofSectionName(sectionName);
				section.setSectionSourceClassName(sectionSourceClassName);
				section.setSectionLocation(sectionLocation);

				// Refactor the properties
				section.getProperties().clear();
				if (properties != null) {
					for (Property property : properties) {
						section.addProperty(new PropertyModel(property
								.getName(), property.getValue()));
					}
				}
			}

			@Override
			public void revert() {
				// Revert attributes
				section.setWoofSectionName(existingSectionName);
				section.setSectionSourceClassName(existingSectionSourceClassName);
				section.setSectionLocation(existingSectionLocation);

				// Revert the properties
				section.getProperties().clear();
				for (PropertyModel property : existingProperties) {
					section.addProperty(property);
				}
			}
		};
		changes.add(attributeChange);

		// Obtain the mapping of existing inputs
		Map<String, WoofSectionInputModel> existingInputNameMapping = new HashMap<String, WoofSectionInputModel>();
		for (WoofSectionInputModel input : section.getInputs()) {
			existingInputNameMapping
					.put(input.getWoofSectionInputName(), input);
		}

		// Refactor the inputs (either refactoring, adding or removing)
		for (final SectionInputType inputType : sectionType
				.getSectionInputTypes()) {

			// Obtain the mapped section input model
			final String inputName = inputType.getSectionInputName();
			String mappedInputName = sectionInputNameMapping.get(inputName);
			final WoofSectionInputModel existingInputModel = existingInputNameMapping
					.remove(mappedInputName);

			// Obtain further type details
			final String parameterType = inputType.getParameterType();

			// Determine action to take based on existing input
			Change<WoofSectionInputModel> sectionInputChange;
			if (existingInputModel != null) {
				// Create change to refactor existing input
				final String existingInputName = existingInputModel
						.getWoofSectionInputName();
				final String existingParameterType = existingInputModel
						.getParameterType();
				sectionInputChange = new AbstractChange<WoofSectionInputModel>(
						existingInputModel, "Refactor Section Input") {
					@Override
					public void apply() {
						existingInputModel.setWoofSectionInputName(inputName);
						existingInputModel.setParameterType(parameterType);

						// Rename connections links
						this.renameConnections(sectionName, inputName);
					}

					@Override
					public void revert() {
						existingInputModel
								.setWoofSectionInputName(existingInputName);
						existingInputModel
								.setParameterType(existingParameterType);

						// Revert connection links
						this.renameConnections(existingSectionName,
								existingInputName);
					}

					/**
					 * Renames the {@link WoofSectionInputModel} connection
					 * names.
					 * 
					 * @param sectionName
					 *            {@link WoofSectionModel} name.
					 */
					private void renameConnections(String sectionName,
							String inputName) {
						for (WoofSectionInputModel input : section.getInputs()) {

							// Rename exception connections
							for (WoofExceptionToWoofSectionInputModel conn : input
									.getWoofExceptions()) {
								conn.setSectionName(sectionName);
								conn.setInputName(inputName);
							}

							// Rename section output connections
							for (WoofSectionOutputToWoofSectionInputModel conn : input
									.getWoofSectionOutputs()) {
								conn.setSectionName(sectionName);
								conn.setInputName(inputName);
							}

							// Rename start connections
							for (WoofStartToWoofSectionInputModel conn : input
									.getWoofStarts()) {
								conn.setSectionName(sectionName);
								conn.setInputName(inputName);
							}

							// Rename template connections
							for (WoofTemplateOutputToWoofSectionInputModel conn : input
									.getWoofTemplateOutputs()) {
								conn.setSectionName(sectionName);
								conn.setInputName(inputName);
							}
						}
					}
				};

			} else {
				// Create change to add input (with no URI)
				final WoofSectionInputModel newInputModel = new WoofSectionInputModel(
						inputName, parameterType, null);
				sectionInputChange = new AbstractChange<WoofSectionInputModel>(
						newInputModel, "Add Section Input") {
					@Override
					public void apply() {
						section.addInput(newInputModel);
					}

					@Override
					public void revert() {
						section.removeInput(newInputModel);
					}
				};
			}
			changes.add(sectionInputChange);
		}
		for (final WoofSectionInputModel unmappedInputModel : existingInputNameMapping
				.values()) {
			// Create change to remove the unmapped input model
			Change<WoofSectionInputModel> unmappedInputChange = new AbstractChange<WoofSectionInputModel>(
					unmappedInputModel, "Remove Section Input") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnections(unmappedInputModel.getWoofExceptions(),
							list);
					removeConnections(
							unmappedInputModel.getWoofSectionOutputs(), list);
					removeConnections(unmappedInputModel.getWoofStarts(), list);
					removeConnections(
							unmappedInputModel.getWoofTemplateOutputs(), list);
					this.connections = list.toArray(new ConnectionModel[list
							.size()]);

					// Remove the section input
					section.removeInput(unmappedInputModel);
				}

				@Override
				public void revert() {

					// Add input back to section
					section.addInput(unmappedInputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedInputChange);
		}

		// Obtain the mapping of existing outputs
		Map<String, WoofSectionOutputModel> existingOutputNameMapping = new HashMap<String, WoofSectionOutputModel>();
		for (WoofSectionOutputModel output : section.getOutputs()) {
			existingOutputNameMapping.put(output.getWoofSectionOutputName(),
					output);
		}

		// Refactor the ouputs (either refactoring, adding or removing)
		for (final SectionOutputType outputType : sectionType
				.getSectionOutputTypes()) {

			// Ignore escalations
			if (outputType.isEscalationOnly()) {
				continue;
			}

			// Obtain the mapped section output model
			final String outputName = outputType.getSectionOutputName();
			String mappedOutputName = sectionOutputNameMapping.get(outputName);
			final WoofSectionOutputModel existingOutputModel = existingOutputNameMapping
					.remove(mappedOutputName);

			// Obtain further type details
			final String argumentType = outputType.getArgumentType();

			// Determine action to take based on existing output
			Change<WoofSectionOutputModel> sectionOutputChange;
			if (existingOutputModel != null) {
				// Create change to refactor existing output
				final String existingOutputName = existingOutputModel
						.getWoofSectionOutputName();
				final String existingArgumentType = existingOutputModel
						.getArgumentType();
				sectionOutputChange = new AbstractChange<WoofSectionOutputModel>(
						existingOutputModel, "Refactor Section Output") {
					@Override
					public void apply() {
						existingOutputModel
								.setWoofSectionOutputName(outputName);
						existingOutputModel.setArgumentType(argumentType);
					}

					@Override
					public void revert() {
						existingOutputModel
								.setWoofSectionOutputName(existingOutputName);
						existingOutputModel
								.setArgumentType(existingArgumentType);
					}
				};

			} else {
				// Create change to add output (with no URI)
				final WoofSectionOutputModel newOutputModel = new WoofSectionOutputModel(
						outputName, argumentType);
				sectionOutputChange = new AbstractChange<WoofSectionOutputModel>(
						newOutputModel, "Add Section Output") {
					@Override
					public void apply() {
						section.addOutput(newOutputModel);
					}

					@Override
					public void revert() {
						section.removeOutput(newOutputModel);
					}
				};
			}
			changes.add(sectionOutputChange);
		}
		for (final WoofSectionOutputModel unmappedOutputModel : existingOutputNameMapping
				.values()) {
			// Create change to remove the unmapped output model
			Change<WoofSectionOutputModel> unmappedOutputChange = new AbstractChange<WoofSectionOutputModel>(
					unmappedOutputModel, "Remove Section Output") {

				/**
				 * {@link ConnectionModel} instances removed.
				 */
				private ConnectionModel[] connections;

				@Override
				public void apply() {

					// Remove the connections
					List<ConnectionModel> list = new LinkedList<ConnectionModel>();
					removeConnection(unmappedOutputModel.getWoofResource(),
							list);
					removeConnection(unmappedOutputModel.getWoofSectionInput(),
							list);
					removeConnection(unmappedOutputModel.getWoofTemplate(),
							list);
					this.connections = list.toArray(new ConnectionModel[list
							.size()]);

					// Remove the section output
					section.removeOutput(unmappedOutputModel);
				}

				@Override
				public void revert() {

					// Add output back to section
					section.addOutput(unmappedOutputModel);

					// Add back in connections
					reconnectConnections(this.connections);
				}
			};
			changes.add(unmappedOutputChange);
		}

		// Sort inputs/outputs at end (so apply has right order)
		changes.add(sortChange);

		// Return aggregate change for refactoring
		return new AggregateChange<WoofSectionModel>(section,
				"Refactor Section", changes.toArray(new Change[changes.size()]));
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
					removeConnections(input.getWoofStarts(), list);
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
	public Change<WoofGovernanceModel> refactorGovernance(
			final WoofGovernanceModel governance, String governanceName,
			final String governanceSourceClassName,
			final PropertyList properties, GovernanceType<?, ?> governanceType) {

		// Ensure governance available to remove
		boolean isInModel = false;
		for (WoofGovernanceModel model : this.model.getWoofGovernances()) {
			if (model == governance) {
				isInModel = true;
			}
		}
		if (!isInModel) {
			// Governance model not in model
			return new NoChange<WoofGovernanceModel>(
					governance,
					"Refactor governance " + governance.getWoofGovernanceName(),
					"Governance " + governance.getWoofGovernanceName()
							+ " is not in WoOF model");
		}

		// Obtain the existing details
		final String existingGovernanceName = governance
				.getWoofGovernanceName();
		final String existingGovernanceSourceClassName = governance
				.getGovernanceSourceClassName();
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(
				governance.getProperties());

		// Obtain the unique governance name
		final String uniqueGovernanceName = getUniqueName(governanceName,
				governance, this.model.getWoofGovernances(),
				GOVERNANCE_NAME_EXTRACTOR);

		// Return change to refactor governance
		return new AbstractChange<WoofGovernanceModel>(governance,
				"Refactor Governance") {

			@Override
			public void apply() {
				// Apply attribute changes
				governance.setWoofGovernanceName(uniqueGovernanceName);
				governance
						.setGovernanceSourceClassName(governanceSourceClassName);

				// Apply property changes
				governance.getProperties().clear();
				if (properties != null) {
					for (Property property : properties) {
						governance.addProperty(new PropertyModel(property
								.getName(), property.getValue()));
					}
				}

				// Update order of governances (after name change)
				WoofChangesImpl.this.sortGovernances();
			}

			@Override
			public void revert() {
				// Revert attributes
				governance.setWoofGovernanceName(existingGovernanceName);
				governance
						.setGovernanceSourceClassName(existingGovernanceSourceClassName);

				// Revert properties
				governance.getProperties().clear();
				for (PropertyModel property : existingProperties) {
					governance.addProperty(property);
				}

				// Update order of governances (after name revert)
				WoofChangesImpl.this.sortGovernances();
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
	public Change<WoofResourceModel> refactorResource(
			WoofResourceModel resource, String resourcePath) {
		return this.changeResourcePath(resource, resourcePath,
				"Refactor Resource");
	}

	@Override
	public Change<WoofResourceModel> changeResourcePath(
			final WoofResourceModel resource, final String resourcePath) {
		return this.changeResourcePath(resource, resourcePath,
				"Change Resource Path");
	}

	/**
	 * Changes the path for the {@link WoofResourceModel}.
	 * 
	 * @param resource
	 *            {@link WoofResourceModel} to have is path changed.
	 * @param resourcePath
	 *            New path for the {@link WoofResourceModel}.
	 * @param changeDescription
	 *            Description of type of {@link Change}.
	 * @return {@link Change} to the path for the {@link WoofResourceModel}.
	 */
	private Change<WoofResourceModel> changeResourcePath(
			final WoofResourceModel resource, final String resourcePath,
			String changeDescription) {

		// No change if no resource path
		if (CompileUtil.isBlank(resourcePath)) {
			return new NoChange<WoofResourceModel>(resource, changeDescription,
					"Must provide resource path");
		}

		// Track original values
		final String originalName = resource.getWoofResourceName();
		final String originalPath = resource.getResourcePath();

		// Obtain the resource name after the resource path
		final String newName = getUniqueName(resourcePath, resource,
				this.model.getWoofResources(), RESOURCE_NAME_EXTRACTOR);

		// Return change to resource path
		return new AbstractChange<WoofResourceModel>(resource,
				changeDescription) {
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
	public Change<WoofExceptionModel> refactorException(
			final WoofExceptionModel exception, final String exceptionClassName) {

		// Determine if the exception has already been handled
		boolean isAlreadyHandled = false;
		for (WoofExceptionModel model : this.model.getWoofExceptions()) {

			// Ignore the exception being refactored
			if (model == exception) {
				continue; // ignore
			}

			// Determine if handling exception
			if (model.getClassName().equals(exceptionClassName)) {
				isAlreadyHandled = true;
			}
		}
		if (isAlreadyHandled) {
			// Exception already handled
			return new NoChange<WoofExceptionModel>(exception,
					"Refactor Exception", "Exception " + exceptionClassName
							+ " is already handled");
		}

		// Obtain the existing exception class name (for revert)
		final String existingExceptionClassName = exception.getClassName();

		// Return change to refactor exception
		return new AbstractChange<WoofExceptionModel>(exception,
				"Refactor Exception") {
			@Override
			public void apply() {
				exception.setClassName(exceptionClassName);
			}

			@Override
			public void revert() {
				exception.setClassName(existingExceptionClassName);
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
	public Change<WoofStartModel> addStart() {

		// Create the start
		final WoofStartModel start = new WoofStartModel();

		// Return change to add start
		return new AbstractChange<WoofStartModel>(start, "Add Start") {
			@Override
			public void apply() {
				WoofChangesImpl.this.model.addWoofStart(start);
			}

			@Override
			public void revert() {
				WoofChangesImpl.this.model.removeWoofStart(start);
			}
		};
	}

	@Override
	public Change<WoofStartModel> removeStart(final WoofStartModel start) {

		// Return change to remove start
		return new AbstractChange<WoofStartModel>(start, "Remove start") {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> list = new LinkedList<ConnectionModel>();
				removeConnection(start.getWoofSectionInput(), list);
				this.connections = list
						.toArray(new ConnectionModel[list.size()]);

				// Remove the start
				WoofChangesImpl.this.model.removeWoofStart(start);
			}

			@Override
			public void revert() {
				// Add back the start
				WoofChangesImpl.this.model.addWoofStart(start);
				reconnectConnections(this.connections);
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

	@Override
	public Change<WoofStartToWoofSectionInputModel> linkStartToSectionInput(
			WoofStartModel start, WoofSectionInputModel sectionInput) {

		// Obtain the containing section
		WoofSectionModel section = this.getSection(sectionInput);
		if (section == null) {
			return new NoChange<WoofStartToWoofSectionInputModel>(
					new WoofStartToWoofSectionInputModel(),
					"The section input '"
							+ sectionInput.getWoofSectionInputName()
							+ "' was not found");
		}

		// Create the connection
		final WoofStartToWoofSectionInputModel connection = new WoofStartToWoofSectionInputModel(
				section.getWoofSectionName(),
				sectionInput.getWoofSectionInputName(), start, sectionInput);

		// Return change to add connection
		return new AddLinkChange<WoofStartToWoofSectionInputModel, WoofStartModel>(
				connection, start, "Link Start to Section Input") {
			@Override
			protected void addExistingConnections(WoofStartModel source,
					List<ConnectionModel> list) {
				list.add(source.getWoofSectionInput());
			}
		};
	}

	@Override
	public Change<WoofStartToWoofSectionInputModel> removeStartToSectionInput(
			WoofStartToWoofSectionInputModel link) {
		return new RemoveLinkChange<WoofStartToWoofSectionInputModel>(link,
				"Remove Start to Section Input");
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