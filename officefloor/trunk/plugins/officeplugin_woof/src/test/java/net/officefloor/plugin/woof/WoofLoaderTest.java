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
package net.officefloor.plugin.woof;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.gwt.web.http.section.GwtHttpTemplateSectionExtension;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSectionExtension;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;

/**
 * Tests the {@link WoofLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ignores failures to load {@link WoofTemplateExtensionService} instances.
	 */
	public static void ignoreExtensionServiceFailures() {
		new WoofLoaderTest().setUp();
	}

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader = Thread.currentThread()
			.getContextClassLoader();

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context = new ClassLoaderConfigurationContext(
			this.classLoader);

	/**
	 * {@link WoofRepository}.
	 */
	private final WoofRepository repository = new WoofRepositoryImpl(
			new ModelRepositoryImpl());

	/**
	 * {@link WoofLoader} to test.
	 */
	private final WoofLoader loader = new WoofLoaderImpl(this.classLoader,
			this.context, this.repository);

	/**
	 * Mock {@link WebAutoWireApplication}.
	 */
	private final WebAutoWireApplication app = this
			.createMock(WebAutoWireApplication.class);

	/**
	 * {@link Logger}.
	 */
	private final Logger logger = Logger.getLogger(WoofLoaderImpl.class
			.getName());

	/**
	 * {@link LogRecord} instances.
	 */
	private final List<LogRecord> logRecords = new LinkedList<LogRecord>();

	/**
	 * {@link Handler} instances.
	 */
	private final List<Handler> logHandlers = new LinkedList<Handler>();

	@Override
	protected void setUp() {

		// Set up to intercept all logging
		this.logger.setUseParentHandlers(false);

		// Remove the existing handlers
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
			this.logHandlers.add(handler);
		}

		// Add handler to intercept message
		this.logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				synchronized (WoofLoaderTest.this.logRecords) {
					WoofLoaderTest.this.logRecords.add(record);
				}
			}

			@Override
			public void flush() {
				// Do nothing
			}

			@Override
			public void close() throws SecurityException {
				// Do nothing
			}
		});
	}

	@Override
	protected void tearDown() throws Exception {

		// Reinstate handlers and logger state
		this.logger.setUseParentHandlers(true);
		for (Handler handler : this.logger.getHandlers()) {
			this.logger.removeHandler(handler);
		}
		for (Handler handler : this.logHandlers) {
			this.logger.addHandler(handler);
		}

		// Validate warned failed to load unknown service
		synchronized (this.logRecords) {
			assertEquals("Should warn of service failure", 1,
					this.logRecords.size());
			LogRecord record = this.logRecords.get(0);
			assertEquals(
					"Incorrect cause message",
					WoofTemplateExtensionService.class.getName()
							+ ": Provider woof.template.extension.not.available.Service not found",
					record.getThrown().getMessage());
		}
	}

	/**
	 * Ensure can load configuration to {@link WebAutoWireApplication}.
	 */
	public void testLoading() throws Exception {

		final HttpTemplateAutoWireSection templateA = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSection templateB = this
				.createMock(HttpTemplateAutoWireSection.class);
		final AutoWireSection sectionA = this.createMock(AutoWireSection.class);
		final AutoWireSection sectionB = this.createMock(AutoWireSection.class);

		// Record loading templates
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/TemplateA.ofp", Template.class, "example"), templateA);
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/TemplateB.ofp", Template.class, null), templateB);

		// Record loading sections
		this.recordReturn(this.app, this.app.addSection("SECTION_A",
				ClassSectionSource.class, Section.class.getName()), sectionA);
		sectionA.addProperty("name.one", "value.one");
		sectionA.addProperty("name.two", "value.two");
		this.app.linkUri("example", sectionA, "INPUT_B");
		this.recordReturn(this.app, this.app.addSection("SECTION_B",
				ClassSectionSource.class, Section.class.getName()), sectionB);

		// Record linking template outputs
		this.app.link(templateA, "OUTPUT_1", sectionA, "INPUT_A");
		this.app.linkToHttpTemplate(templateA, "OUTPUT_2", templateB);
		this.app.linkToResource(templateA, "OUTPUT_3", "Example.html");

		// Record linking section outputs
		this.app.link(sectionA, "OUTPUT_A", sectionB, "INPUT_1");
		this.app.linkToHttpTemplate(sectionA, "OUTPUT_B", templateA);
		this.app.linkToResource(sectionA, "OUTPUT_C", "Example.html");

		// Record linking escalations
		this.app.linkEscalation(Exception.class, sectionA, "INPUT_A");
		this.app.linkEscalation(RuntimeException.class, templateA);
		this.app.linkEscalation(SQLException.class, "Example.html");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(
				this.getFileLocation(this.getClass(), "application.woof"),
				this.app);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load GWT extension.
	 */
	public void testGwt() throws Exception {

		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSectionExtension extension = this
				.createMock(HttpTemplateAutoWireSectionExtension.class);

		// Record loading template
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/Template.html", Template.class, "example"), template);

		// Record extending with GWT
		this.recordReturn(template, template.getTemplateUri(), "example");
		this.recordReturn(template, template
				.addTemplateExtension(GwtHttpTemplateSectionExtension.class),
				extension);
		extension.addProperty(
				GwtHttpTemplateSectionExtension.PROPERTY_TEMPLATE_URI,
				"example");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(
				this.getFileLocation(this.getClass(), "gwt.woof"), this.app);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown template extension.
	 */
	public void testUnknownTemplateExtension() throws Exception {

		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);

		// Record loading template
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/Template.html", Template.class, "example"), template);

		// Should not load further as unknown template extension

		// Test
		this.replayMockObjects();
		try {
			this.loader.loadWoofConfiguration(this.getFileLocation(
					this.getClass(), "unknown-template-extension.woof"),
					this.app);
			fail("Should not load successfully");
		} catch (WoofTemplateExtensionException ex) {
			assertEquals("Incorrect exception",
					"Failed loading Template Extension UNKNOWN",
					ex.getMessage());
			assertTrue("Incorrect cause",
					ex.getCause() instanceof ClassNotFoundException);
		}
		this.verifyMockObjects();
	}

	/**
	 * Mock template class.
	 */
	public static class Template {
		public void template() {
		}
	}

	/**
	 * Mock section class.
	 */
	public static class Section {
		public void taskOne() {
		}
	}

}