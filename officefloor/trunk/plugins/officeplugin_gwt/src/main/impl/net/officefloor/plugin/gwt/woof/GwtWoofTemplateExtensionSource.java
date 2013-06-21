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
package net.officefloor.plugin.gwt.woof;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.model.change.Change;
import net.officefloor.model.gwt.module.GwtModuleModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.woof.WoofChangeIssues;
import net.officefloor.plugin.gwt.comet.web.http.section.CometHttpTemplateSectionExtension;
import net.officefloor.plugin.gwt.module.GwtChanges;
import net.officefloor.plugin.gwt.module.GwtChangesImpl;
import net.officefloor.plugin.gwt.module.GwtFailureListener;
import net.officefloor.plugin.gwt.module.GwtModule;
import net.officefloor.plugin.gwt.module.GwtModuleRepository;
import net.officefloor.plugin.gwt.module.GwtModuleRepositoryImpl;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionChangeContext;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionConfiguration;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.plugin.woof.template.impl.AbstractWoofTemplateExtensionSource;

import com.google.gwt.core.client.EntryPoint;

/**
 * {@link WoofTemplateExtensionSource} for GWT.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtWoofTemplateExtensionSource extends
		AbstractWoofTemplateExtensionSource {

	/**
	 * Name of the property for the GWT {@link EntryPoint} class name.
	 */
	public static String PROPERTY_GWT_ENTRY_POINT_CLASS_NAME = "gwt.entry.point.class.name";

	/**
	 * Name of the property for the GWT Async Service Interfaces.
	 */
	public static String PROPERTY_GWT_ASYNC_SERVICE_INTERFACES = GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES;

	/**
	 * Name of property to indicate if GWT Comet is enabled.
	 */
	public static String PROPERTY_ENABLE_COMET = "gwt.comet.enable";

	/**
	 * Prefix on the {@link GwtModule} path to identify it within the
	 * {@link ConfigurationContext}.
	 */
	private static String GWT_MODULE_PATH_PREFIX = "src/main/resources";

	/**
	 * Creates the {@link GwtModule} path.
	 * 
	 * @param templateUri
	 *            URI for the {@link HttpTemplateAutoWireSection}.
	 * @param gwtEntryPointClassName
	 *            GWT {@link EntryPoint} class name.
	 * @return {@link GwtModule} path.
	 */
	public static String createGwtModulePath(String templateUri,
			String gwtEntryPointClassName) {

		// Create the module
		GwtModuleModel module = new GwtModuleModel(templateUri,
				gwtEntryPointClassName, null);

		// Create the GWT repository
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		GwtModuleRepository gwtRepository = new GwtModuleRepositoryImpl(
				new ModelRepositoryImpl(), classLoader, GWT_MODULE_PATH_PREFIX);

		// Return the GWT Module path
		return gwtRepository.createGwtModulePath(module);
	}

	/**
	 * Creates the {@link GwtModuleModel}.
	 * 
	 * @param configuration
	 *            {@link WoofTemplateExtensionConfiguration}.
	 * @param inherit
	 *            Inherit GWT Modules for {@link GwtModuleModel}.
	 * @return {@link GwtModuleModel}.
	 */
	private static GwtModuleModel createGetModule(
			WoofTemplateExtensionConfiguration configuration, String... inherit) {

		// No path if no configuration
		if (configuration == null) {
			return null;
		}

		// Obtain the configuration details
		String uri = configuration.getUri();
		String entryPointClassName = configuration.getProperty(
				PROPERTY_GWT_ENTRY_POINT_CLASS_NAME, null);

		// Ensure have both URI and Entry Point class
		if ((CompileUtil.isBlank(uri))
				|| (CompileUtil.isBlank(entryPointClassName))) {
			return null;
		}

		// Create the GWT Module
		GwtModuleModel module = new GwtModuleModel(uri, entryPointClassName,
				inherit);

		// Return the GWT Module
		return module;
	}

	/**
	 * Obtains the GWT Module path.
	 * 
	 * @param configuration
	 *            {@link WoofTemplateExtensionConfiguration}. May be
	 *            <code>null</code>.
	 * @param gwtRepository
	 *            {@link GwtModuleRepository}.
	 * @return GWT Module path or <code>null</code> if can not determine path.
	 */
	private static String getGwtModulePath(
			WoofTemplateExtensionConfiguration configuration,
			GwtModuleRepository gwtRepository) {

		// Obtain the GWT Module
		GwtModuleModel module = createGetModule(configuration);
		if (module == null) {
			return null; // No module, so no path
		}

		// Obtain the GWT module path
		String gwtModulePath = gwtRepository.createGwtModulePath(module);

		// Return the GWT module path
		return gwtModulePath;
	}

	/**
	 * Determines if Comet is enabled.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @return <code>true</code> if Comet is enabled.
	 */
	private static boolean isCometEnabled(SourceProperties properties) {
		String enableComet = properties.getProperty(PROPERTY_ENABLE_COMET,
				String.valueOf(false));
		return Boolean.TRUE.equals(Boolean.valueOf(enableComet));
	}

	/*
	 * ==================== WoofTemplateExtensionSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_GWT_ENTRY_POINT_CLASS_NAME,
				"GWT EntryPoint Class");
	}

	@Override
	public Change<?> createConfigurationChange(
			WoofTemplateExtensionChangeContext context) {

		// Do nothing if no new (deleting)
		WoofTemplateExtensionConfiguration newConfiguration = context
				.getNewConfiguration();
		if (newConfiguration == null) {
			return null; // No change
		}

		// Obtain the context details
		ClassLoader classLoader = context.getClassLoader();
		ConfigurationContext configurationContext = context
				.getConfigurationContext();

		// Create the GWT repository
		GwtModuleRepository gwtRepository = new GwtModuleRepositoryImpl(
				new ModelRepositoryImpl(), classLoader, GWT_MODULE_PATH_PREFIX);

		// Create the failure listener
		final WoofChangeIssues issues = context.getWoofChangeIssues();
		GwtFailureListener listener = new GwtFailureListener() {
			@Override
			public void notifyFailure(String message, Throwable cause) {
				issues.addIssue(message, cause);
			}
		};

		// Create GWT changes
		GwtChanges gwtChanges = new GwtChangesImpl(gwtRepository,
				configurationContext, listener);

		// Create the module
		GwtModuleModel gwtModule = createGetModule(newConfiguration);

		// Determine if enhance with Comet
		if (isCometEnabled(newConfiguration)) {
			CometHttpTemplateSectionExtension.extendGwtModule(gwtModule);
		}

		// Obtain the existing module path
		String existingGwtModulePath = getGwtModulePath(
				context.getOldConfiguration(), gwtRepository);

		// Create the change to create/update the GWT Module
		Change<?> change = gwtChanges.updateGwtModule(gwtModule,
				existingGwtModulePath);

		// Return the change
		return change;
	}

	@Override
	public void extendTemplate(WoofTemplateExtensionSourceContext context)
			throws Exception {

		// Extend template with GWT
		GwtHttpTemplateSectionExtension.extendTemplate(context.getTemplate(),
				context.getWebApplication(), context, context.getClassLoader());

		// Extend template with GWT Comet (if enabled)
		if (isCometEnabled(context)) {
			CometHttpTemplateSectionExtension.extendTemplate(
					context.getTemplate(), context.getWebApplication(),
					context, context.getClassLoader());
		}
	}

}