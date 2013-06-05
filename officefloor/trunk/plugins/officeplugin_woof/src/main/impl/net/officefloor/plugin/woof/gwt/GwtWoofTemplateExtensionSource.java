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
package net.officefloor.plugin.woof.gwt;

import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofTemplateExtensionModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.comet.web.http.section.CometHttpTemplateSectionExtension;
import net.officefloor.plugin.gwt.module.GwtChanges;
import net.officefloor.plugin.gwt.module.GwtChangesImpl;
import net.officefloor.plugin.gwt.module.GwtFailureListener;
import net.officefloor.plugin.gwt.module.GwtModuleRepositoryImpl;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionChangeContext;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.plugin.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * {@link WoofTemplateExtensionSource} for GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtWoofTemplateExtensionSource extends
		AbstractWoofTemplateExtensionSource {

	/**
	 * Name of the extension property for the GWT Module path.
	 */
	public static String PROPERTY_GWT_MODULE_PATH = "gwt.module.path";

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
	// TODO remove and possibly only provide generic retrieval for extensions
	@Deprecated
	private static WoofTemplateExtensionModel getGwtTemplateExtension(
			WoofTemplateModel template) {
		return getTemplateExtension(template,
				GwtWoofTemplateExtensionSource.class.getName());
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
	 * Obtains the GWT entry point class name for the {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @param gwtChanges
	 *            {@link GwtChanges}.
	 * @return GWT entry point class name or <code>null</code>.
	 */
	public static String getGwtEntryPointClassName(WoofTemplateModel template,
			GwtChanges gwtChanges) {

		// Obtain the GWT module path
		WoofTemplateExtensionModel gwtExtension = getGwtTemplateExtension(template);
		PropertyModel gwtModulePathProperty = getGwtModulePathProperty(gwtExtension);
		String gwtModulePath = getPropertyValue(gwtModulePathProperty);
		if (gwtModulePath != null) {

			// Obtain the GWT module
			GwtModuleModel gwtModule = gwtChanges
					.retrieveGwtModule(gwtModulePath);
			if (gwtModule != null) {

				// Return the EntryPoint class name
				return gwtModule.getEntryPointClassName();
			}
		}

		// As here, no GWT EntryPoint class name
		return null;
	}

	/**
	 * Obtains the GWT Async Service Interfaces for the
	 * {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return GWT Async Service Interfaces.
	 */
	public static String[] getGwtAsyncServiceInterfaceNames(
			WoofTemplateModel template) {
		WoofTemplateExtensionModel gwtExtension = getGwtTemplateExtension(template);
		PropertyModel gwtAsyncInterfacesProperty = getGwtAsyncInterfacesProperty(gwtExtension);
		String gwtAsyncInterfaceValue = getPropertyValue(gwtAsyncInterfacesProperty);
		String[] gwtAsyncServiceInterfaceNames = GwtHttpTemplateSectionExtension
				.getGwtAsyncServiceInterfaceNames(gwtAsyncInterfaceValue);
		return gwtAsyncServiceInterfaceNames;
	}

	/**
	 * Indicates if Comet is enabled for the {@link WoofTemplateModel}.
	 * 
	 * @param template
	 *            {@link WoofTemplateModel}.
	 * @return <code>true</code> Comet is enabled for the
	 *         {@link WoofTemplateModel}.
	 */
	public static boolean isCometEnabled(WoofTemplateModel template) {

		// TODO move GWT and Comet functionality to extension
		throw new UnsupportedOperationException(
				"TODO move GWT and Comet functionality to extension");

		// WoofTemplateExtensionModel cometExtension =
		// getCometTemplateExtension(template);
		// return (cometExtension != null);
	}

	/**
	 * Obtains the Comet manual publish method name.
	 * 
	 * @param template
	 *            {@link WoofTemplateExtensionModel}.
	 * @return Comet manual publish method name or <code>null</code>.
	 */
	public static String getCometManualPublishMethodName(
			WoofTemplateModel template) {

		// TODO move GWT and Comet functionality to extension
		throw new UnsupportedOperationException(
				"TODO move GWT and Comet functionality to extension");

		// WoofTemplateExtensionModel cometExtension =
		// getCometTemplateExtension(template);
		// PropertyModel cometManualPublishMethodProperty =
		// getCometManualPublishMethodProperty(cometExtension);
		// return getPropertyValue(cometManualPublishMethodProperty);
	}

	/**
	 * Creates the {@link GwtChanges}.
	 * 
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @return {@link GwtChanges}.
	 */
	private GwtChanges createGwtChanges(ConfigurationContext context) {

		// TODO create GWT changes
		ClassLoader classLoader = null;
		GwtFailureListener listener = null;
		GwtChanges gwtChanges = new GwtChangesImpl(new GwtModuleRepositoryImpl(
				new ModelRepositoryImpl(), classLoader, "src/main/resources"),
				context, listener);

		// Return the GWT changes
		return gwtChanges;
	}

	/*
	 * ==================== WoofTemplateExtensionSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_GWT_MODULE_PATH, "GWT Module Path");
	}

	@Override
	public Change<?> createConfigurationChange(
			WoofTemplateExtensionChangeContext context) {

		// TODO Obtain details
		String gwtEntryPointClassName = null;
		String uri = context.getNewConfiguration().getUri();
		boolean isEnableComet = false;

		// TODO create GWT changes
		GwtChanges gwtChanges = this.createGwtChanges(context
				.getConfigurationContext());

		// ++++++++++ ADD +++++++++++++++++++++++

		// Determine if have GWT Extension
		if ((gwtEntryPointClassName != null)
				&& (gwtEntryPointClassName.trim().length() > 0)) {

			// Create the GWT Module
			GwtModuleModel module = new GwtModuleModel(uri,
					gwtEntryPointClassName, null);

			// Include inherits for Comet (if using)
			if (isEnableComet) {
				// Extend GWT Module for Comet
				CometHttpTemplateSectionExtension.extendGwtModule(module);
			}

			// Obtain the GWT Module path
			String gwtModulePath = gwtChanges.createGwtModulePath(module);

			// Create change for adding GWT Module
			Change<?> gwtChange = gwtChanges.updateGwtModule(module, null);

			// Return change to add GWT Module
			return gwtChange;
		}

		// ++++++++++ REFACTOR +++++++++++++++++++++++

		// // Obtain the GWT extension (and GWT Module Path)
		// WoofTemplateExtensionModel gwtExtension =
		// getGwtTemplateExtension(template);
		// PropertyModel gwtModulePathProperty =
		// getGwtModulePathProperty(gwtExtension);
		// final String existingGwtModulePath =
		// getPropertyValue(gwtModulePathProperty);
		// PropertyModel gwtAsyncInterfacesProperty =
		// getGwtAsyncInterfacesProperty(gwtExtension);
		// final String existingGwtAsyncInterfaces =
		// getPropertyValue(gwtAsyncInterfacesProperty);
		//
		// // Create the GWT Module and associated property values
		// GwtModuleModel gwtModule = null;
		// if (gwtEntryPointClassName != null) {
		// // Create the GWT Module and new property values
		// gwtModule = new GwtModuleModel(uri, gwtEntryPointClassName, null);
		// if (isEnableComet) {
		// // Extend potential GWT Module for Comet
		// CometHttpTemplateSectionExtension.extendGwtModule(gwtModule);
		// }
		// }
		// final String newGwtModulePath = (gwtModule == null ? null :
		// gwtChanges
		// .createGwtModulePath(gwtModule));
		// final String newGwtAsyncInterfaces =
		// getGwtAsyncServicesPropertyValue(gwtServiceAsyncInterfaceNames);
		//
		// // Provide details for GWT Module refactoring
		// boolean isUpdateGwtModule = false;
		// final WoofTemplateExtensionModel finalGwtExtension = gwtExtension;
		// final boolean hasExistingGwtModulePathProperty;
		// final PropertyModel finalGwtModulePathProperty;
		// if (gwtModulePathProperty == null) {
		// // No existing GWT Module Path property
		// hasExistingGwtModulePathProperty = false;
		// finalGwtModulePathProperty = new PropertyModel(
		// PROPERTY_GWT_MODULE_PATH, newGwtModulePath);
		// } else {
		// // Has existing GWT Module Path property
		// hasExistingGwtModulePathProperty = true;
		// finalGwtModulePathProperty = gwtModulePathProperty;
		// }
		// final boolean hasExistingGwtAsyncServicesProperty;
		// final PropertyModel finalGwtAsyncServicesProperty;
		// if (gwtAsyncInterfacesProperty == null) {
		// // No existing GWT Async Services property
		// hasExistingGwtAsyncServicesProperty = false;
		// finalGwtAsyncServicesProperty = new PropertyModel(
		// GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
		// newGwtAsyncInterfaces);
		// } else {
		// // Has existing GWT Async Services property
		// hasExistingGwtAsyncServicesProperty = true;
		// finalGwtAsyncServicesProperty = gwtAsyncInterfacesProperty;
		// }
		//
		// // Refactor the GWT extension (either refactoring, adding or
		// removing)
		// boolean isExistingEntryPointClass = (gwtExtension != null);
		// boolean isRefactoringToHaveEntryPointClass = (gwtEntryPointClassName
		// != null)
		// && (gwtEntryPointClassName.trim().length() > 0);
		// if (isExistingEntryPointClass) {
		// // Existing to refactor or remove
		// if (isRefactoringToHaveEntryPointClass) {
		// // Refactor the GWT extension (and set to update GWT Module)
		// isUpdateGwtModule = true;
		// Change<WoofTemplateExtensionModel> change = new
		// AbstractChange<WoofTemplateExtensionModel>(
		// finalGwtExtension, "Refactor GWT Extension") {
		// @Override
		// public void apply() {
		//
		// // Refactor GWT Module Path
		// finalGwtModulePathProperty.setValue(newGwtModulePath);
		// if (!hasExistingGwtModulePathProperty) {
		// finalGwtExtension
		// .addProperty(finalGwtModulePathProperty);
		// }
		//
		// // Refactor GWT Async Services
		// finalGwtAsyncServicesProperty
		// .setValue(newGwtAsyncInterfaces);
		// if (!hasExistingGwtAsyncServicesProperty) {
		// finalGwtExtension
		// .addProperty(finalGwtAsyncServicesProperty);
		// }
		// }
		//
		// @Override
		// public void revert() {
		//
		// // Refactor GWT Module Path
		// finalGwtModulePathProperty
		// .setValue(existingGwtModulePath);
		// if (!hasExistingGwtModulePathProperty) {
		// finalGwtExtension
		// .removeProperty(finalGwtModulePathProperty);
		// }
		//
		// // Refactor GWT Async Services
		// finalGwtAsyncServicesProperty
		// .setValue(existingGwtAsyncInterfaces);
		// if (!hasExistingGwtAsyncServicesProperty) {
		// finalGwtExtension
		// .removeProperty(finalGwtAsyncServicesProperty);
		// }
		// }
		// };
		// changes.add(change);
		//
		// } else {
		// // Remove the GWT extension
		// Change<WoofTemplateExtensionModel> change = new
		// AbstractChange<WoofTemplateExtensionModel>(
		// finalGwtExtension, "Remove GWT Extension") {
		// @Override
		// public void apply() {
		// template.removeExtension(finalGwtExtension);
		// }
		//
		// @Override
		// public void revert() {
		// template.addExtension(finalGwtExtension);
		// }
		// };
		// changes.add(change);
		// }
		//
		// } else if (isRefactoringToHaveEntryPointClass) {
		// // Add the GWT extension (and set to update GWT Module)
		// isUpdateGwtModule = true;
		// final WoofTemplateExtensionModel addGwtExtension = new
		// WoofTemplateExtensionModel(
		// GwtWoofTemplateExtensionSource.EXTENSION_ALIAS);
		// addGwtExtension.addProperty(finalGwtModulePathProperty);
		// addGwtExtension.addProperty(finalGwtAsyncServicesProperty);
		// Change<WoofTemplateExtensionModel> change = new
		// AbstractChange<WoofTemplateExtensionModel>(
		// addGwtExtension, "Add GWT Extension") {
		// @Override
		// public void apply() {
		// template.addExtension(addGwtExtension);
		// }
		//
		// @Override
		// public void revert() {
		// template.removeExtension(addGwtExtension);
		// }
		// };
		// changes.add(change);
		// }
		//
		// // Add change to refactor the GWT Module (if required)
		// if (isUpdateGwtModule) {
		// changes.add(this.gwtChanges.updateGwtModule(gwtModule,
		// existingGwtModulePath));
		// }
		//
		// // Obtain the Comet extension (and manual publish property)
		// final WoofTemplateExtensionModel cometExtension =
		// getCometTemplateExtension(template);
		// PropertyModel cometManualPublishProperty =
		// getCometManualPublishMethodProperty(cometExtension);
		// final String existingCometManualPublishMethodName =
		// getPropertyValue(cometManualPublishProperty);
		//
		// // Provide details for Comet refactoring
		// final boolean hasExistingCometManualPathPathProperty;
		// final PropertyModel finalCometManualPathProperty;
		// if (cometManualPublishProperty == null) {
		// // No existing Comet manual publish property
		// hasExistingCometManualPathPathProperty = false;
		// finalCometManualPathProperty = new PropertyModel(
		// CometHttpTemplateSectionExtension.PROPERTY_MANUAL_PUBLISH_METHOD_NAME,
		// cometManualPublishMethodName);
		// } else {
		// // Has existing Comet manual publish property
		// hasExistingCometManualPathPathProperty = true;
		// finalCometManualPathProperty = cometManualPublishProperty;
		// }
		//
		// // Refactor the Comet extension (either refactoring, adding or
		// removing)
		// boolean isExistingCometExtension = (cometExtension != null);
		// if (isExistingCometExtension) {
		// // Existing to refactor or remove
		// if (isEnableComet) {
		// // Refactor Comet extension
		// Change<WoofTemplateExtensionModel> change = new
		// AbstractChange<WoofTemplateExtensionModel>(
		// cometExtension, "Refactor Comet Extension") {
		// @Override
		// public void apply() {
		// // Refactor manual publish
		// finalCometManualPathProperty
		// .setValue(cometManualPublishMethodName);
		// if (!hasExistingCometManualPathPathProperty) {
		// cometExtension
		// .addProperty(finalCometManualPathProperty);
		// }
		// }
		//
		// @Override
		// public void revert() {
		// // Revert manual publish
		// finalCometManualPathProperty
		// .setValue(existingCometManualPublishMethodName);
		// if (!hasExistingCometManualPathPathProperty) {
		// cometExtension
		// .removeProperty(finalCometManualPathProperty);
		// }
		// }
		// };
		// changes.add(change);
		//
		// } else {
		// // Remove Comet extension
		// Change<WoofTemplateExtensionModel> change = new
		// AbstractChange<WoofTemplateExtensionModel>(
		// cometExtension, "Remove Comet Extension") {
		// @Override
		// public void apply() {
		// template.removeExtension(cometExtension);
		// }
		//
		// @Override
		// public void revert() {
		// template.addExtension(cometExtension);
		// }
		// };
		// changes.add(change);
		// }
		//
		// } else if (isEnableComet) {
		// // Create Comet extension to add
		// final WoofTemplateExtensionModel addCometExtension = new
		// WoofTemplateExtensionModel(
		// CometWoofTemplateExtensionService.EXTENSION_ALIAS);
		// if ((cometManualPublishMethodName != null)
		// && (cometManualPublishMethodName.trim().length() > 0)) {
		// // Add manual publish method name
		// addCometExtension.addProperty(finalCometManualPathProperty);
		// }
		//
		// // Add Comet extension
		// Change<WoofTemplateExtensionModel> change = new
		// AbstractChange<WoofTemplateExtensionModel>(
		// cometExtension, "Add Comet Extension") {
		// @Override
		// public void apply() {
		// template.addExtension(addCometExtension);
		// }
		//
		// @Override
		// public void revert() {
		// template.removeExtension(addCometExtension);
		// }
		// };
		// changes.add(change);
		// }
		//
		// // ++++++++++ CHANGE URI +++++++++++++++++++++
		//
		// // Obtain GWT Extension and GWT Module path property
		// WoofTemplateExtensionModel gwtExtension =
		// getGwtTemplateExtension(template);
		// final PropertyModel gwtModuleProperty =
		// getGwtModulePathProperty(gwtExtension);
		// final String existingGwtModulePath =
		// getPropertyValue(gwtModuleProperty);
		//
		// // Retrieve the new GWT Module path (keeping existing if not found)
		// GwtModuleModel gwtModule = null;
		// if (existingGwtModulePath != null) {
		// gwtModule = this.gwtChanges
		// .retrieveGwtModule(existingGwtModulePath);
		// gwtModule.setRenameTo(uri);
		// }
		// final String newGwtModulePath = (gwtModule == null ? null
		// : this.gwtChanges.createGwtModulePath(gwtModule));
		//
		// // Create change to template URI
		// Change<WoofTemplateModel> change = new
		// AbstractChange<WoofTemplateModel>(
		// template, "Change Template URI") {
		// @Override
		// public void apply() {
		//
		// // Update GWT (if able to update)
		// if ((gwtModuleProperty != null) && (newGwtModulePath != null)) {
		// gwtModuleProperty.setValue(newGwtModulePath);
		// }
		// }
		//
		// @Override
		// public void revert() {
		//
		// // Revert GWT (if able to revert)
		// if (gwtModuleProperty != null) {
		// gwtModuleProperty.setValue(existingGwtModulePath);
		// }
		// }
		// };
		//
		// // Include change to update GWT Module
		// if (existingGwtModulePath != null) {
		// Change<GwtModuleModel> gwtChange = this.gwtChanges.updateGwtModule(
		// gwtModule, existingGwtModulePath);
		// change = new AggregateChange<WoofTemplateModel>(change.getTarget(),
		// change.getChangeDescription(), change, gwtChange);
		// }
		//
		// // TODO comet --------------------------------
		//
		// // Extend the template
		// CometHttpTemplateSectionExtension.extendTemplate(context.getTemplate(),
		// context.getWebApplication(), context, context.getClassLoader());
		//
		// // Configure in as extension to HTTP template
		// context.getTemplate().addTemplateExtension(
		// CometHttpTemplateSectionExtension.class);

		// TODO provide change
		return null;
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

	@Override
	public void extendTemplate(WoofTemplateExtensionSourceContext context)
			throws Exception {

		// Extend template with GWT
		GwtHttpTemplateSectionExtension.extendTemplate(context.getTemplate(),
				context.getWebApplication(), context, context.getClassLoader());

		// Extend template with GWT Comet
		CometHttpTemplateSectionExtension.extendTemplate(context.getTemplate(),
				context.getWebApplication(), context, context.getClassLoader());

		// Configure in as extension to HTTP template
		context.getTemplate().addTemplateExtension(
				CometHttpTemplateSectionExtension.class);
	}

}