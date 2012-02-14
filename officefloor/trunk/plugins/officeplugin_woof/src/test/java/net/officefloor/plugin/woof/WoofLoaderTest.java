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

package net.officefloor.plugin.woof;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.comet.CometPublisher;
import net.officefloor.plugin.comet.section.CometSectionSource;
import net.officefloor.plugin.comet.spi.CometService;
import net.officefloor.plugin.comet.web.http.section.CometHttpTemplateSectionExtension;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
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
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler
			.newOfficeFloorCompiler(null);

	/**
	 * {@link WoofLoader} to test.
	 */
	private final WoofLoader loader = new WoofLoaderImpl(
			new WoofRepositoryImpl(new ModelRepositoryImpl()));

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
		final AutoWireGovernance governanceA = this
				.createMock(AutoWireGovernance.class);
		final AutoWireGovernance governanceB = this
				.createMock(AutoWireGovernance.class);

		// Record obtaining compiler
		this.recordReturn(this.app, this.app.getOfficeFloorCompiler(),
				this.compiler);

		// Record loading templates
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/TemplateA.ofp", Template.class, "example"), templateA);
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/TemplateB.ofp", Template.class, null), templateB);

		// Record loading sections
		this.recordReturn(
				this.app,
				this.app.addSection("SECTION_A",
						ClassSectionSource.class.getName(),
						Section.class.getName()), sectionA);
		sectionA.addProperty("name.one", "value.one");
		sectionA.addProperty("name.two", "value.two");
		this.app.linkUri("example", sectionA, "INPUT_B");
		this.recordReturn(
				this.app,
				this.app.addSection("SECTION_B", "CLASS",
						Section.class.getName()), sectionB);

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
		
		// Record linking starts
		this.app.addStartupFlow(sectionA, "INPUT_A");
		this.app.addStartupFlow(sectionB, "INPUT_1");

		// Record loading governances
		this.recordReturn(this.app, this.app.addGovernance("GOVERNANCE_A",
				ClassGovernanceSource.class.getName()), governanceA);
		governanceA.addProperty("name.a", "value.a");
		governanceA.addProperty("name.b", "value.b");
		governanceA.governSection(templateA);
		governanceA.governSection(sectionA);
		this.recordReturn(this.app,
				this.app.addGovernance("GOVERNANCE_B", "CLASS"), governanceB);

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(
				this.getConfiguration("application.woof"), this.app);
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

		// Record obtaining compiler
		this.recordReturn(this.app, this.app.getOfficeFloorCompiler(),
				this.compiler);

		// Record loading template
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/Template.html", Template.class, "example"), template);

		// Record extending with GWT
		this.recordReturn(this.app, this.app.isObjectAvailable(new AutoWire(
				ServerGwtRpcConnection.class)), true);
		this.recordReturn(template, template.getTemplateUri(), "example");
		this.recordReturn(template, template
				.addTemplateExtension(GwtHttpTemplateSectionExtension.class),
				extension);
		extension.addProperty(
				GwtHttpTemplateSectionExtension.PROPERTY_TEMPLATE_URI,
				"example");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(this.getConfiguration("gwt.woof"),
				this.app);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load Comet extension.
	 */
	public void testComet() throws Exception {

		final AutoWireSection cometSection = this
				.createMock(AutoWireSection.class);
		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSectionExtension extension = this
				.createMock(HttpTemplateAutoWireSectionExtension.class);

		// Record obtaining compiler
		this.recordReturn(this.app, this.app.getOfficeFloorCompiler(),
				this.compiler);

		// Record loading template
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/Template.html", Template.class, "example"), template);

		// Record extending with GWT
		this.recordReturn(this.app,
				this.app.isObjectAvailable(new AutoWire(CometService.class)),
				true);
		this.recordReturn(this.app,
				this.app.isObjectAvailable(new AutoWire(CometPublisher.class)),
				true);
		this.recordReturn(this.app, this.app.getSection("COMET"), cometSection);
		this.recordReturn(template, template.getTemplateUri(), "example");
		this.app.linkUri("example/comet-subscribe", cometSection,
				CometSectionSource.SUBSCRIBE_INPUT_NAME);
		this.app.linkUri("example/comet-publish", cometSection,
				CometSectionSource.PUBLISH_INPUT_NAME);
		this.recordReturn(template, template.getTemplateLogicClass(),
				Template.class);
		this.recordReturn(template, template
				.addTemplateExtension(CometHttpTemplateSectionExtension.class),
				extension);

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(this.getConfiguration("comet.woof"),
				this.app);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown template extension.
	 */
	public void testUnknownTemplateExtension() throws Exception {

		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);

		// Record obtaining compiler
		this.recordReturn(this.app, this.app.getOfficeFloorCompiler(),
				this.compiler);

		// Record loading template
		this.recordReturn(this.app, this.app.addHttpTemplate(
				"WOOF/Template.html", Template.class, "example"), template);

		// Should not load further as unknown template extension

		// Test
		this.replayMockObjects();
		try {
			this.loader.loadWoofConfiguration(
					this.getConfiguration("unknown-template-extension.woof"),
					this.app);
			fail("Should not load successfully");
		} catch (WoofTemplateExtensionException ex) {
			assertEquals(
					"Incorrect exception",
					"Failed loading Template Extension UNKNOWN. "
							+ new ClassNotFoundException("UNKNOWN")
									.getMessage(), ex.getMessage());
			assertTrue("Incorrect cause",
					ex.getCause() instanceof ClassNotFoundException);
		}
		this.verifyMockObjects();
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem getConfiguration(String fileName)
			throws Exception {
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				this.compiler.getClassLoader());
		ConfigurationItem configuration = context
				.getConfigurationItem(location);
		assertNotNull("Can not find configuration '" + fileName + "'",
				configuration);
		return configuration;
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