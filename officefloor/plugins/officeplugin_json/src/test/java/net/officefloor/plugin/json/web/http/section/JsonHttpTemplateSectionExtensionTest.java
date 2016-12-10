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
package net.officefloor.plugin.json.web.http.section;

import java.io.Serializable;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.json.HttpJson;
import net.officefloor.plugin.json.JsonResponseWriter;
import net.officefloor.plugin.json.read.JsonRequestReaderManagedObjectSource;
import net.officefloor.plugin.json.write.JsonResponseWriterManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link JsonHttpTemplateSectionExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class JsonHttpTemplateSectionExtensionTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpTemplateAutoWireSection}.
	 */
	private final HttpTemplateAutoWireSection template = this
			.createMock(HttpTemplateAutoWireSection.class);

	/**
	 * {@link WebAutoWireApplication}.
	 */
	public final WebAutoWireApplication application = this
			.createMock(WebAutoWireApplication.class);

	/**
	 * {@link HttpTemplateSectionExtensionContext}.
	 */
	private final HttpTemplateSectionExtensionContext context = this
			.createMock(HttpTemplateSectionExtensionContext.class);

	/**
	 * Ensure not extend {@link HttpTemplateAutoWireSection} as no logic class.
	 */
	public void testNoLogicClass() throws Exception {
		this.recordGetTemplateLogicClass(null);
		this.doExtend();
	}

	/**
	 * Ensure not extend {@link HttpTemplateAutoWireSection} if no JSON inline
	 * configuration.
	 */
	public void testNoJson() throws Exception {
		this.recordGetTemplateLogicClass(NoJsonLogic.class);
		this.doExtend();
	}

	/**
	 * Template logic without JSON functionality.
	 */
	public static class NoJsonLogic {
		public void service(ServerHttpConnection connection) {
		}
	}

	/**
	 * Ensure load {@link JsonRequestReaderManagedObjectSource} for
	 * {@link HttpJson}.
	 */
	public void testHttpJson() throws Exception {

		this.recordGetTemplateLogicClass(HttpJsonLogic.class);

		// Record configuring the HttpJson object
		this.recordReturn(this.application, this.application
				.isObjectAvailable(new AutoWire(HttpJsonRequest.class)), false);
		AutoWireObject jsonObject = this.recordAddManagedObject(
				JsonRequestReaderManagedObjectSource.class,
				HttpJsonRequest.class);
		jsonObject
				.addProperty(
						JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
						HttpJsonRequest.class.getName());

		// Test
		this.doExtend();
	}

	/**
	 * Ensure not load {@link JsonRequestReaderManagedObjectSource} for
	 * {@link HttpJson} on object already configured.
	 */
	public void testHttpJsonExists() throws Exception {

		this.recordGetTemplateLogicClass(HttpJsonLogic.class);

		// Record HttpJson object already available
		this.recordReturn(this.application, this.application
				.isObjectAvailable(new AutoWire(HttpJsonRequest.class)), true);

		// Test
		this.doExtend();
	}

	/**
	 * {@link JsonRequestReaderManagedObjectSource} in-line configuration.
	 */
	@HttpJson
	public static class HttpJsonRequest implements Serializable {
	}

	/**
	 * Template logic with {@link HttpJson} in-line configuration.
	 */
	public static class HttpJsonLogic {
		public void service(HttpJsonRequest request) {
		}
	}

	/**
	 * Ensure load {@link JsonRequestReaderManagedObjectSource} for
	 * {@link HttpJson} using the bound name.
	 */
	public void testBoundHttpJson() throws Exception {

		this.recordGetTemplateLogicClass(BoundHttpJsonLogic.class);

		// Record configuring the HttpJson object
		this.recordReturn(this.application, this.application
				.isObjectAvailable(new AutoWire(BoundHttpJsonRequest.class)),
				false);
		AutoWireObject jsonObject = this.recordAddManagedObject(
				JsonRequestReaderManagedObjectSource.class,
				BoundHttpJsonRequest.class);
		jsonObject
				.addProperty(
						JsonRequestReaderManagedObjectSource.PROPERTY_JSON_OBJECT_CLASS,
						BoundHttpJsonRequest.class.getName());
		jsonObject.addProperty(
				JsonRequestReaderManagedObjectSource.PROPERTY_BIND_NAME,
				"BOUND");

		// Test
		this.doExtend();
	}

	/**
	 * {@link JsonRequestReaderManagedObjectSource} in-line configuration with
	 * bound name.
	 */
	@HttpJson("BOUND")
	public static class BoundHttpJsonRequest implements Serializable {
	}

	/**
	 * Template logic with {@link HttpJson} in-line configuration for bound
	 * name.
	 */
	public static class BoundHttpJsonLogic {
		public void service(BoundHttpJsonRequest request) {
		}
	}

	/**
	 * Ensure appropriately add {@link JsonResponseWriterManagedObjectSource}
	 * and flag method not to render.
	 */
	public void testJsonResponseWriter() throws Exception {

		this.recordGetTemplateLogicClass(JsonResponseWriterLogic.class);

		// Record configuring the JsonResponseWriter
		this.recordReturn(this.application, this.application
				.isObjectAvailable(new AutoWire(JsonResponseWriter.class)),
				false);
		this.recordAddManagedObject(
				JsonResponseWriterManagedObjectSource.class,
				JsonResponseWriter.class);

		// Record the JSON AJX servicing methods
		this.doExtend("ajaxJsonService", "anotherAjaxJsonService");
	}

	/**
	 * Ensure not add {@link JsonResponseWriterManagedObjectSource} as already
	 * configured.
	 */
	public void testJsonResponseWriterExists() throws Exception {

		this.recordGetTemplateLogicClass(JsonResponseWriterLogic.class);

		// Record JsonResponseWriter already configured
		this.recordReturn(this.application, this.application
				.isObjectAvailable(new AutoWire(JsonResponseWriter.class)),
				true);

		// Record the JSON AJX servicing methods
		this.doExtend("ajaxJsonService", "anotherAjaxJsonService");
	}

	/**
	 * Template logic with {@link JsonResponseWriter}.
	 */
	public static class JsonResponseWriterLogic {

		public void ajaxJsonService(JsonResponseWriter writer) {
		}

		public void anotherAjaxJsonService(ServerHttpConnection connection,
				JsonResponseWriter writer) {
		}

		public void nonAjaxJsonService(ServerHttpConnection connection) {
		}
	}

	/**
	 * Records obtaining the {@link HttpTemplateAutoWireSection} logic class.
	 * 
	 * @param logicClass
	 *            {@link HttpTemplateAutoWireSection} logic class.
	 */
	private void recordGetTemplateLogicClass(Class<?> logicClass) {
		this.recordReturn(this.template, this.template.getTemplateLogicClass(),
				logicClass);
	}

	/**
	 * Records adding a {@link AutoWireObject}.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param objectType
	 *            {@link AutoWire} object type.
	 * @return {@link AutoWireObject}.
	 */
	private AutoWireObject recordAddManagedObject(
			Class<?> managedObjectSourceClass, Class<?> objectType) {

		final AutoWireObject object = this.createMock(AutoWireObject.class);

		// Record adding the managed object
		this.recordReturn(this.application, this.application.addManagedObject(
				managedObjectSourceClass.getName(), null, new AutoWire(
						objectType)), object, new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect source", expected[0], actual[0]);
				assertEquals("Incorrect wirer", expected[1], actual[1]);
				AutoWire[] actualAutoWires = (AutoWire[]) actual[2];
				assertEquals("Incorrect number of auto wires", 1,
						actualAutoWires.length);
				AutoWire[] expectedAutoWires = (AutoWire[]) expected[2];
				assertEquals("Incorrect auto wire", expectedAutoWires[0],
						actualAutoWires[0]);
				return true;
			}
		});

		// Return the object
		return object;
	}

	/**
	 * Undertakes the extending of the {@link HttpTemplateAutoWireSection}.
	 */
	private void doExtend(String... jsonAjaxMethods) throws Exception {

		// Provide the JSON AJAX service method configuration
		boolean isExtendTemplate = (jsonAjaxMethods.length > 0);
		if (isExtendTemplate) {

			final HttpTemplateAutoWireSectionExtension extension = this
					.createMock(HttpTemplateAutoWireSectionExtension.class);

			// Record adding the JSON HTTP template extension
			this.recordReturn(
					this.template,
					this.template
							.addTemplateExtension(JsonHttpTemplateSectionExtension.class),
					extension);

			// Create the extension method names property value
			StringBuilder methodNamesBuilder = new StringBuilder();
			boolean isFirst = true;
			for (String jsonAjaxMethod : jsonAjaxMethods) {
				if (!isFirst) {
					methodNamesBuilder.append(",");
				}
				isFirst = false;
				methodNamesBuilder.append(jsonAjaxMethod);
			}
			String methodNames = methodNamesBuilder.toString();

			// Add the method names property
			extension
					.addProperty(
							JsonHttpTemplateSectionExtension.PROPERTY_JSON_AJAX_METHOD_NAMES,
							methodNames);

			// Record obtaining the method names property
			this.recordReturn(
					this.context,
					this.context
							.getProperty(JsonHttpTemplateSectionExtension.PROPERTY_JSON_AJAX_METHOD_NAMES),
					methodNames);

			// Record flagging the JSON AJAX methods to not render template
			for (String jsonAjaxMethod : jsonAjaxMethods) {
				this.context.flagAsNonRenderTemplateMethod(jsonAjaxMethod);
			}
		}

		// Test
		this.replayMockObjects();

		// Extends the template configuration
		JsonHttpTemplateSectionExtension.extendTemplate(this.template,
				this.application);

		// Extend template initiation
		if (isExtendTemplate) {
			new JsonHttpTemplateSectionExtension().extendTemplate(this.context);
		}

		// Verify functionality
		this.verifyMockObjects();
	}

}