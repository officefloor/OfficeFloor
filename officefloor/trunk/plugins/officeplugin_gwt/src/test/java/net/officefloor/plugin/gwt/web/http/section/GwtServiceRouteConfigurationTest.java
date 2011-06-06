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
package net.officefloor.plugin.gwt.web.http.section;

import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;

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
	 * Ensure no extension of configuration if no GWT services.
	 */
	public void testNoGwtService() {

		// No GWT Services
		this.recordInit("template", null);

		// Test configuration
		this.replayMockObjects();
		GwtHttpTemplateSectionExtension.extendTemplate(this.template);
		this.verifyMockObjects();
	}

	/**
	 * Ensure extend configuration to route GWT service request.
	 */
	public void testGwtService() {

		// Record configuring a GWT Service
		this.recordInit("template", GwtServiceInterface.class.getName());
		this.application.linkUri("template/GwtServicePath", this.template,
				"GWT_GwtServicePath");

		// Test configuration
		this.replayMockObjects();
		GwtHttpTemplateSectionExtension.extendTemplate(this.template);
		this.verifyMockObjects();
	}

	/**
	 * Ensure extend configuration to route multiple GWT services.
	 */
	public void testMultipleGwtServices() {

		// Record configuring multiple GWT services
		this.recordInit("template", GwtServiceInterface.class.getName() + ","
				+ GwtServiceAnother.class.getName());
		this.application.linkUri("template/GwtServicePath", this.template,
				"GWT_Another");
		this.application.linkUri("template/Another", this.template,
				"GWT_Another");

		// Test configuration
		this.replayMockObjects();
		GwtHttpTemplateSectionExtension.extendTemplate(this.template);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if GWT service is not annotated with
	 * {@link RemoteServiceRelativePath}.
	 */
	public void testNonAnnotatedGwtService() {

		// Record configuring non-annotated GWT service
		this.recordInit("template", Object.class.getName());

		// Test configuration
		this.replayMockObjects();
		try {
			GwtHttpTemplateSectionExtension.extendTemplate(this.template);
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals(
					"Incorrect cause",
					"GWT Service Interface java.lang.Object is not annotated with RemoteServiceRelativePath",
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

		final HttpTemplateAutoWireSectionExtension extension = this
				.createMock(HttpTemplateAutoWireSectionExtension.class);

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
	 * GWT Service interface.
	 */
	@RemoteServiceRelativePath("GwtServicePath")
	public static interface GwtServiceInterface {
	}

	/**
	 * Another GWT Service interface.
	 */
	@RemoteServiceRelativePath("Another")
	public static interface GwtServiceAnother {
	}

}