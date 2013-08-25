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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofChangeIssues;
import net.officefloor.plugin.gwt.comet.CometPublisher;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicer;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.comet.web.http.section.CometHttpTemplateSectionExtension;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.HttpUriLink;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoaderImpl;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionLoaderUtil;

import com.google.gwt.core.client.EntryPoint;

/**
 * Tests the {@link GwtWoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtWoofTemplateExtensionSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final MemoryConfigurationContext context = new MemoryConfigurationContext();

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		WoofTemplateExtensionLoaderUtil
				.validateSpecification(
						GwtWoofTemplateExtensionSource.class,
						GwtWoofTemplateExtensionSource.PROPERTY_GWT_ENTRY_POINT_CLASS_NAME,
						"GWT EntryPoint Class");
	}

	/**
	 * Ensure {@link NoChange} if blank template URI.
	 */
	public void testRefactor_BlankTemplateUri() throws Exception {

		// No template URI
		Change<?> change = this.doRefactor(null, null, "", null, null, false);

		// Ensure no change (reported via template validation)
		assertFalse("Should not be able to apply", change.canApply());
		Conflict[] conflicts = change.getConflicts();
		assertEquals("Incorrect number of conflicts", 1, conflicts.length);
		assertEquals("Incorrect cause",
				"Must specify template URI for use by extension "
						+ GwtWoofTemplateExtensionSource.class.getName(),
				conflicts[0].getConflictDescription());
	}

	/**
	 * Ensure {@link NoChange} if no {@link EntryPoint} class name.
	 */
	public void testRefactor_NoEntryPointClassName() throws Exception {

		// No entry point class name
		Change<?> change = this
				.doRefactor(null, null, "NEW", null, null, false);

		// Ensure issue
		assertFalse("Should not be able to apply", change.canApply());
		Conflict[] conflicts = change.getConflicts();
		assertEquals("Incorrect number of conflicts", 1, conflicts.length);
		assertEquals("Incorrect cause",
				"Must specify EntryPoint class for extension "
						+ GwtWoofTemplateExtensionSource.class.getName(),
				conflicts[0].getConflictDescription());
	}

	/**
	 * Ensure can add the extension.
	 */
	public void testRefactor_AddExtension() throws Exception {

		final String gwtEntryPointClassName = "net.officefloor.client.Created";
		final String gwtModulePath = "net/officefloor/created.gwt.xml";

		// Create the GWT configuration
		Change<?> change = this.doRefactor(null, null, "created",
				gwtEntryPointClassName, null, false);

		// Validate the change
		this.assertChange(change, null, null, gwtModulePath, "created.gwt.xml");
	}

	/**
	 * Ensure can add extension when GWT Module already exists.
	 */
	public void testRefactor_AddExtensionExisting() throws Exception {

		final String gwtModulePath = "net/officefloor/created.gwt.xml";
		final String gwtEntryPointClassName = "net.officefloor.client.Created";

		// Load the existing configuration
		this.loadConfigurationItem(gwtModulePath, "existing.gwt.xml");

		// Create the GWT configuration
		Change<?> change = this.doRefactor(null, null, "created",
				gwtEntryPointClassName, null, false);

		// Validate the change
		this.assertChange(change, gwtModulePath, "existing.gwt.xml",
				gwtModulePath, "created.gwt.xml");
	}

	/**
	 * Ensure can add the extension for root template.
	 */
	public void testRefactor_AddExtensionForRoot() throws Exception {

		final String gwtEntryPointClassName = "net.officefloor.client.Root";
		final String gwtModulePath = "net/officefloor/root.gwt.xml";

		// Create the GWT configuration
		Change<?> change = this.doRefactor(null, null, "/",
				gwtEntryPointClassName, null, false);

		// Validate the change
		this.assertChange(change, null, null, gwtModulePath, "root.gwt.xml");
	}

	/**
	 * Ensure may extend with comet.
	 */
	public void testRefactor_Comet() throws Exception {

		final String gwtModulePath = "net/officefloor/Comet.gwt.xml";
		final String gwtEntryPointClassName = "net.officefloor.client.Created";

		// Create the GWT configuration
		Change<?> change = this.doRefactor(null, null, "Comet",
				gwtEntryPointClassName, null, true);

		// Validate the change
		this.assertChange(change, null, null, gwtModulePath, "comet.gwt.xml");
	}

	/**
	 * Ensure can update the extension keeping GWT Module configuration in same
	 * place.
	 */
	public void testRefactor_UpdateExtensionInPlace() throws Exception {

		final String gwtModulePath = "net/officefloor/updated.gwt.xml";
		final String existingEntryPointClassName = "net.officefloor.client.Existing";
		final String updatedEntryPointClassName = "net.officefloor.client.Update";

		// Load the existing configuration
		this.loadConfigurationItem(gwtModulePath, "existing.gwt.xml");

		// Create the GWT configuration
		Change<?> change = this.doRefactor("updated",
				existingEntryPointClassName, "updated",
				updatedEntryPointClassName, null, false);

		// Validate the change
		this.assertChange(change, gwtModulePath, "existing.gwt.xml",
				gwtModulePath, "updated.gwt.xml");
	}

	/**
	 * Ensure can update the extension relocating the GWT Module configuration.
	 */
	public void testRefactor_UpdateRelocatingExtension() throws Exception {

		final String existingGwtModulePath = "net/officefloor/existing.gwt.xml";
		final String existingEntryPointClassName = "net.officefloor.client.Existing";
		final String updatedGwtModulePath = "net/officefloor/updated.gwt.xml";
		final String updatedEntryPointClassName = "net.officefloor.client.Update";

		// Load the existing configuration
		this.loadConfigurationItem(existingGwtModulePath, "existing.gwt.xml");

		// Create the GWT configuration
		Change<?> change = this.doRefactor("existing",
				existingEntryPointClassName, "updated",
				updatedEntryPointClassName, null, false);

		// Validate the change
		this.assertChange(change, existingGwtModulePath, "existing.gwt.xml",
				updatedGwtModulePath, "updated.gwt.xml");
	}

	/**
	 * Ensure leave GWT Module on delete.
	 */
	public void testRefactor_DeleteExtension() throws Exception {

		final String gwtModulePath = "net/officefloor/existing.gwt.xml";
		final String gwtEntryPointClassName = "net.officefloor.client.Existing";

		// Load the existing configuration
		this.loadConfigurationItem(gwtModulePath, "existing.gwt.xml");

		// Create the GWT configuration
		Change<?> change = this.doRefactor("existing", gwtEntryPointClassName,
				null, null, null, false);

		// Validate the no change as just leave GWT Module to reuse
		assertNull("Should not have change for delete", change);
	}

	/**
	 * Ensure reports issue in failure of extension.
	 */
	public void testRefactor_Issue() throws Exception {

		final Exception failure = new Exception("TEST");

		final ConfigurationContext configurationContext = this
				.createMock(ConfigurationContext.class);
		final WoofChangeIssues issues = this.createMock(WoofChangeIssues.class);

		// Record failure
		configurationContext
				.getConfigurationItem("src/main/resources/net/officefloor/NEW.gwt.xml");
		this.control(configurationContext).setThrowable(failure);

		// Record reporting of the failure
		issues.addIssue("Template OLD Extension "
				+ GwtWoofTemplateExtensionSource.class.getName()
				+ ": Failure applying GWT Module changes", failure);

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Create the properties
		SourceProperties oldProperties = createSourceProperties(
				"net.officefloor.client.Existing", null, false);
		SourceProperties newProperties = createSourceProperties(
				"net.officefloor.client.Failure", null, false);

		// Create the source context
		SourceContext sourceContext = new SourceContextImpl(false, classLoader);

		// Create the change
		Change<?> change = new WoofTemplateExtensionLoaderImpl()
				.refactorTemplateExtension(
						GwtWoofTemplateExtensionSource.class.getName(), "OLD",
						oldProperties, "NEW", newProperties,
						configurationContext, sourceContext, issues);

		// Test
		this.replayMockObjects();

		// Triggers the failure
		change.apply();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can extend with GWT.
	 */
	public void testExtend_Gwt() throws Exception {

		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);
		final WebAutoWireApplication application = this
				.createMock(WebAutoWireApplication.class);

		// Record the GWT configuration
		this.recordGwtHttpTemplateSectionExtension("uri", template, application);

		// Ensure can extend template with GWT
		this.replayMockObjects();
		WoofTemplateExtensionLoaderUtil
				.extendTemplate(
						GwtWoofTemplateExtensionSource.class,
						template,
						application,
						GwtWoofTemplateExtensionSource.PROPERTY_GWT_ENTRY_POINT_CLASS_NAME,
						"net.officefloor.client.Test");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can extend with GWT and Comet.
	 */
	public void testExtend_GwtAndComet() throws Exception {

		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);
		final WebAutoWireApplication application = this
				.createMock(WebAutoWireApplication.class);
		final HttpTemplateAutoWireSectionExtension extension = this
				.createMock(HttpTemplateAutoWireSectionExtension.class);
		final AutoWireSection section = this.createMock(AutoWireSection.class);
		final HttpUriLink link = this.createMock(HttpUriLink.class);

		// Record the GWT configuration
		this.recordGwtHttpTemplateSectionExtension("uri", template, application);

		// Record the Comet configuration
		this.recordReturn(template, template
				.addTemplateExtension(CometHttpTemplateSectionExtension.class),
				extension);
		this.recordReturn(
				application,
				application.isObjectAvailable(new AutoWire(CometService.class)),
				true);
		this.recordReturn(application, application
				.isObjectAvailable(new AutoWire(CometRequestServicer.class)),
				true);
		this.recordReturn(application, application
				.isObjectAvailable(new AutoWire(CometPublisher.class)), true);
		this.recordReturn(application, application.getSection("COMET"), section);
		this.recordReturn(template, template.getTemplateUri(), "uri");
		this.recordReturn(application, application.linkUri(
				"uri/comet-subscribe", section, "SUBSCRIBE"), link);
		this.recordReturn(application,
				application.linkUri("uri/comet-publish", section, "PUBLISH"),
				link);
		this.recordReturn(template, template.getTemplateLogicClass(),
				Object.class);

		// Ensure can extend template with Comet
		this.replayMockObjects();
		WoofTemplateExtensionLoaderUtil
				.extendTemplate(
						GwtWoofTemplateExtensionSource.class,
						template,
						application,
						GwtWoofTemplateExtensionSource.PROPERTY_GWT_ENTRY_POINT_CLASS_NAME,
						"net.officefloor.client.Test",
						GwtWoofTemplateExtensionSource.PROPERTY_ENABLE_COMET,
						String.valueOf(true));
		this.verifyMockObjects();
	}

	/**
	 * Records adding the {@link GwtHttpTemplateSectionExtension}.
	 * 
	 * @param uri
	 *            Template URI.
	 * @param template
	 *            Mock {@link HttpTemplateAutoWireSection}.
	 * @param application
	 *            Mock {@link WebAutoWireApplication}.
	 */
	private void recordGwtHttpTemplateSectionExtension(String uri,
			HttpTemplateAutoWireSection template,
			WebAutoWireApplication application) {

		final HttpTemplateAutoWireSectionExtension extension = this
				.createMock(HttpTemplateAutoWireSectionExtension.class);

		// Record configuring the GWT extension
		this.recordReturn(template, template.getTemplateUri(), "uri");
		this.recordReturn(application, application
				.isObjectAvailable(new AutoWire(ServerGwtRpcConnection.class)),
				true);
		this.recordReturn(template, template
				.addTemplateExtension(GwtHttpTemplateSectionExtension.class),
				extension);
		extension.addProperty("template.uri", "uri");
	}

	/**
	 * Undertakes the refactor.
	 * 
	 * @param oldUri
	 *            Old URI.
	 * @param oldGwtEntryPointClassName
	 *            Old GWT {@link EntryPoint} class name.
	 * @param newUri
	 *            new URI.
	 * @param newGwtEntryPointClassName
	 *            New GWT {@link EntryPoint} class name.
	 * @param newAsyncServiceInterfaces
	 *            New Async Service Interfaces.
	 * @param isEnableComet
	 *            Indicates if enable Comet.
	 * @return {@link Change}.
	 */
	private Change<?> doRefactor(String oldUri,
			String oldGwtEntryPointClassName, String newUri,
			String newGwtEntryPointClassName, String newAsyncServiceInterfaces,
			boolean isEnableComet) {

		// Obtain the class loader
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		// Create the properties
		SourceProperties oldProperties = createSourceProperties(
				oldGwtEntryPointClassName, null, false);
		SourceProperties newProperties = createSourceProperties(
				newGwtEntryPointClassName, newAsyncServiceInterfaces,
				isEnableComet);

		// Undertake the refactor
		Change<?> change = WoofTemplateExtensionLoaderUtil
				.refactorTemplateExtension(
						GwtWoofTemplateExtensionSource.class, oldUri,
						oldProperties, newUri, newProperties, this.context,
						classLoader);

		// Return the change
		return change;
	}

	/**
	 * Creates the {@link SourceProperties}.
	 * 
	 * @param gwtEntryPointClassName
	 *            GWT {@link EntryPoint} class name.
	 * @param gwtAsyncServiceInterfaces
	 *            GWT async service interfaces.
	 * @param isEnableComet
	 *            Indicates if enable Comet.
	 * @return {@link SourceProperties}.
	 */
	private static SourceProperties createSourceProperties(
			String gwtEntryPointClassName, String gwtAsyncServiceInterfaces,
			boolean isEnableComet) {

		// Create the properties
		List<String> propertyNameValues = new ArrayList<String>(4);
		if (gwtEntryPointClassName != null) {
			propertyNameValues
					.addAll(Arrays
							.asList(GwtWoofTemplateExtensionSource.PROPERTY_GWT_ENTRY_POINT_CLASS_NAME,
									gwtEntryPointClassName));
		}
		if (gwtAsyncServiceInterfaces != null) {
			propertyNameValues
					.addAll(Arrays
							.asList(GwtWoofTemplateExtensionSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
									gwtAsyncServiceInterfaces));
		}
		if (isEnableComet) {
			propertyNameValues.addAll(Arrays.asList(
					GwtWoofTemplateExtensionSource.PROPERTY_ENABLE_COMET,
					String.valueOf(isEnableComet)));
		}

		// Create and return the source properties
		return WoofTemplateExtensionLoaderUtil
				.createSourceProperties(propertyNameValues
						.toArray(new String[propertyNameValues.size()]));
	}

	/**
	 * Asserts the {@link Change}.
	 * 
	 * @param change
	 *            {@link Change}.
	 * @param existingLocation
	 *            Existing location. May be <code>null</code>.
	 * @param existingContentFileName
	 *            File name containing the expected existing content. May be
	 *            <code>null</code>.
	 * @param newLocation
	 *            New location. May be <code>null</code>.
	 * @param newContentFileName
	 *            File name containing the expected new content. May be
	 *            <code>null</code>.
	 * @throws Exception
	 *             If failure in acessing the configuration.
	 */
	private void assertChange(Change<?> change, String existingLocation,
			String existingContentFileName, String newLocation,
			String newContentFileName) throws Exception {

		// Determine if updating in place
		boolean isUpdateInPlace = (newLocation != null)
				&& (newLocation.equals(existingLocation));

		// Ensure change can apply/revert multiple times
		for (int i = 0; i < 3; i++) {

			// Ensure initial state is as expected
			this.assertConfigurationItem(existingLocation,
					existingContentFileName);
			this.assertConfigurationItem(newLocation,
					isUpdateInPlace ? existingContentFileName : null);

			// Ensure change is correct
			change.apply();
			this.assertConfigurationItem(existingLocation,
					isUpdateInPlace ? newContentFileName : null);
			this.assertConfigurationItem(newLocation, newContentFileName);

			// Ensure revert change is correct (on next iteration)
			change.revert();
		}
	}

	/**
	 * Asserts the {@link ConfigurationItem} contains the correct content.
	 * 
	 * @param location
	 *            Location of the {@link ConfigurationItem}.
	 * @param expectedContentFileName
	 *            Name of file containing the expected {@link ConfigurationItem}
	 *            content.
	 */
	private void assertConfigurationItem(String location,
			String expectedContentFileName) throws Exception {

		// Do nothing if no location
		if (location == null) {
			return;
		}

		// Obtain the reader to the content
		Reader reader = null;
		if (expectedContentFileName != null) {
			// Obtain the expected content
			InputStream expectedContent = this
					.getConfiguration(expectedContentFileName);
			reader = new InputStreamReader(expectedContent);
		}

		// Validate the configuration item
		WoofTemplateExtensionLoaderUtil.validateConfigurationItem(this.context,
				"src/main/resources/" + location, reader);
	}

	/**
	 * Loads the {@link ConfigurationItem}.
	 * 
	 * @param location
	 *            Location.
	 * @param contentsFileName
	 *            File name containing the contents to load.
	 * @throws Exception
	 *             If fails loading {@link ConfigurationItem}.
	 */
	public void loadConfigurationItem(String location, String contentsFileName)
			throws Exception {
		this.context.createConfigurationItem("src/main/resources/" + location,
				this.getConfiguration(contentsFileName));
	}

	/**
	 * Obtains the configuration.
	 * 
	 * @param fileName
	 *            File name.
	 * @return {@link InputStream} to the configuration.
	 */
	private InputStream getConfiguration(String fileName) throws Exception {
		return this.findInputStream(this.getClass(), fileName);
	}

}