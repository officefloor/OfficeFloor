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
package net.officefloor.plugin.gwt.web.http.section;

import org.easymock.AbstractMatcher;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnectionManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.HttpUriLink;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Tests configuring the routing of a GWT service.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtServiceRouteConfigurationTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpTemplateAutoWireSection}.
	 */
	private final HttpTemplateAutoWireSection template = this
			.createMock(HttpTemplateAutoWireSection.class);

	/**
	 * Mock {@link WebAutoWireApplication}.
	 */
	private final WebAutoWireApplication application = this
			.createMock(WebAutoWireApplication.class);

	/**
	 * Mock {@link SourceProperties}.
	 */
	private final SourceProperties properties = this
			.createMock(SourceProperties.class);

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader = Thread.currentThread()
			.getContextClassLoader();

	/**
	 * {@link HttpUriLink}.
	 */
	private final HttpUriLink link = this.createMock(HttpUriLink.class);

	/**
	 * Ensure no extension of configuration if no GWT services.
	 */
	public void testNoGwtService() throws Exception {

		// No GWT Services
		this.recordInit("template", null);

		// Test configuration
		this.replayMockObjects();
		GwtHttpTemplateSectionExtension.extendTemplate(this.template,
				this.application, this.properties, this.classLoader);
		this.verifyMockObjects();
	}

	/**
	 * Ensure extend configuration to route GWT service request.
	 */
	public void testGwtService() throws Exception {

		// Record configuring a GWT Service
		this.recordInit("template", GwtServiceInterfaceAsync.class.getName());
		this.recordReturn(this.application,
				this.application.linkUri("template/GwtServicePath",
						this.template, "GWT_GwtServicePath"), this.link);

		// Test configuration
		this.replayMockObjects();
		GwtHttpTemplateSectionExtension.extendTemplate(this.template,
				this.application, this.properties, this.classLoader);
		this.verifyMockObjects();
	}

	/**
	 * Ensure extend configuration to route GWT service requests for root
	 * template.
	 */
	public void testGwtServiceForRootTemplate() throws Exception {

		// Record configuring a GWT Service
		this.recordInit("/", GwtServiceInterfaceAsync.class.getName());
		this.recordReturn(this.application, this.application.linkUri(
				"root/GwtServicePath", this.template, "GWT_GwtServicePath"),
				this.link);

		// Test configuration
		this.replayMockObjects();
		GwtHttpTemplateSectionExtension.extendTemplate(this.template,
				this.application, this.properties, this.classLoader);
		this.verifyMockObjects();
	}

	/**
	 * Ensure extend configuration to route multiple GWT services.
	 */
	public void testMultipleGwtServices() throws Exception {

		// Record configuring multiple GWT services
		this.recordInit("template", GwtServiceInterfaceAsync.class.getName()
				+ "," + GwtServiceAnotherAsync.class.getName());
		this.recordReturn(this.application,
				this.application.linkUri("template/GwtServicePath",
						this.template, "GWT_GwtServicePath"), this.link);
		this.recordReturn(this.application, this.application.linkUri(
				"template/Another", this.template, "GWT_Another"), this.link);

		// Test configuration
		this.replayMockObjects();
		GwtHttpTemplateSectionExtension.extendTemplate(this.template,
				this.application, this.properties, this.classLoader);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if GWT service is not annotated with
	 * {@link RemoteServiceRelativePath}.
	 */
	public void testNonAnnotatedGwtService() throws Exception {

		// Record configuring non-annotated GWT service
		this.recordInit("template", GwtNoRelativePathAsync.class.getName());

		// Test configuration
		this.replayMockObjects();
		try {
			GwtHttpTemplateSectionExtension.extendTemplate(this.template,
					this.application, this.properties, this.classLoader);
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", "GWT Service Interface "
					+ GwtNoRelativePath.class.getName()
					+ " is not annotated with RemoteServiceRelativePath",
					ex.getMessage());
		}
		this.verifyMockObjects();
	}

	/**
	 * Initiate recordings.
	 * 
	 * @param templateUri
	 *            Template URI.
	 * @param gwtServiceInterfaces
	 *            GWT Service Interfaces. May be <code>null</code>.
	 */
	private void recordInit(String templateUri, String gwtServiceInterfaces) {

		final AutoWireObject serverGwtRpcConnection = this
				.createMock(AutoWireObject.class);
		final HttpTemplateAutoWireSectionExtension extension = this
				.createMock(HttpTemplateAutoWireSectionExtension.class);
		final ManagedObjectSourceWirerContext wirerContext = this
				.createMock(ManagedObjectSourceWirerContext.class);

		// Record adding Server GWT RPC Connection
		this.recordReturn(this.application, this.application
				.isObjectAvailable(new AutoWire(ServerGwtRpcConnection.class)),
				false);
		this.recordReturn(this.application, this.application.addManagedObject(
				ServerGwtRpcConnectionManagedObjectSource.class.getName(),
				null, new AutoWire(ServerGwtRpcConnection.class), new AutoWire(
						AsyncCallback.class)), serverGwtRpcConnection,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect managed object source",
								expected[0], actual[0]);
						ManagedObjectSourceWirer wirer = (ManagedObjectSourceWirer) actual[1];
						assertNotNull("Should be a wirer", wirer);
						wirer.wire(wirerContext);
						AutoWire[] expectedAutoWiring = (AutoWire[]) expected[2];
						AutoWire[] actualAutoWiring = (AutoWire[]) actual[2];
						assertEquals("Incorrect number of auto-wiring",
								expectedAutoWiring.length,
								actualAutoWiring.length);
						for (int i = 0; i < expectedAutoWiring.length; i++) {
							assertEquals("Incorrect auto-wire " + i,
									expectedAutoWiring[i], actualAutoWiring[i]);
						}
						return true;
					}
				});
		wirerContext.setManagedObjectScope(ManagedObjectScope.PROCESS);

		// Record configuring a GWT Service
		this.recordReturn(this.template, this.template.getTemplateUri(),
				templateUri);
		this.recordReturn(this.template, this.template
				.addTemplateExtension(GwtHttpTemplateSectionExtension.class),
				extension);
		extension.addProperty(
				GwtHttpTemplateSectionExtension.PROPERTY_TEMPLATE_URI,
				templateUri);

		// Record providing the GWT Service interfaces
		this.recordReturn(
				this.properties,
				this.properties
						.getProperty(
								GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
								null), gwtServiceInterfaces);
		if (gwtServiceInterfaces != null) {
			extension
					.addProperty(
							GwtHttpTemplateSectionExtension.PROPERTY_GWT_ASYNC_SERVICE_INTERFACES,
							gwtServiceInterfaces);
		}
	}

	/**
	 * GWT Async Service interface.
	 */
	public static interface GwtServiceInterfaceAsync {
	}

	/**
	 * GWT Service interface.
	 */
	@RemoteServiceRelativePath("GwtServicePath")
	public static interface GwtServiceInterface {
	}

	/**
	 * Another GWT Async Service interface.
	 */
	@RemoteServiceRelativePath("Another")
	public static interface GwtServiceAnotherAsync {
	}

	/**
	 * No relative path GWT Async Service.
	 */
	public static interface GwtNoRelativePathAsync {
	}

	/**
	 * No relative path GWT Service.
	 */
	public static interface GwtNoRelativePath {
	}

}