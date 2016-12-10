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
package net.officefloor.plugin.servlet.webxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.plugin.servlet.filter.configuration.FilterInstance;
import net.officefloor.plugin.servlet.filter.configuration.FilterMappings;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.security.HttpServletSecurity;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Tests the {@link WebXmlSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebXmlSectionSourceTest extends AbstractWebXmlTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(WebXmlSectionSource.class);
	}

	/**
	 * Validate type via loading from section location.
	 */
	public void testTypeViaSectionLocation() throws Exception {

		// Create the expected type
		SectionDesigner type = this.createExpectedType();

		// Validate type
		String webXmlLocation = this.getPackageRelativePath(this.getClass()) + "/Type.xml";
		SectionLoaderUtil.validateSectionType(type, WebXmlSectionSource.class, webXmlLocation,
				WebXmlSectionSource.PROPERTY_WEB_XML_CONFIGURATION, "This should be ignored");
	}

	/**
	 * Validate type via loading from {@link Property}.
	 */
	public void testTypeViaProperty() throws Exception {

		// Create the expected type
		SectionDesigner type = this.createExpectedType();

		// Load the configuration
		String configuration = this.getFileContents(this.findFile(this.getClass(), "Type.xml"));

		// Validate type
		SectionLoaderUtil.validateSectionType(type, WebXmlSectionSource.class, null,
				WebXmlSectionSource.PROPERTY_WEB_XML_CONFIGURATION, configuration);
	}

	/**
	 * Creates the expected type.
	 * 
	 * @return Expected type.
	 */
	private SectionDesigner createExpectedType() throws Exception {

		// Create the expected type
		SectionDesigner type = SectionLoaderUtil.createSectionDesigner();
		type.addSectionInput("service", null);
		type.addSectionOutput("unhandled", null, false);
		type.addSectionOutput(ServletException.class.getSimpleName(), ServletException.class.getName(), true);
		type.addSectionOutput(IOException.class.getSimpleName(), IOException.class.getName(), true);
		type.addSectionObject("SERVLET_SERVER", ServletServer.class.getName());
		type.addSectionObject("HTTP_CONNECTION", ServerHttpConnection.class.getName());
		type.addSectionObject("REQUEST_ATTRIBUTES", HttpRequestState.class.getName());
		type.addSectionObject("HTTP_SESSION", HttpSession.class.getName());
		type.addSectionObject("HTTP_SECURITY", HttpServletSecurity.class.getName());

		// Return the expected type
		return type;
	}

	/**
	 * Ensure can determine if valid XML configuration.
	 */
	public void testValidWebXmlConfiguration() throws Exception {

		// Create the source context
		SourceContext context = new SourceContextImpl(false, Thread.currentThread().getContextClassLoader());

		// Validate marker file
		this.undertakeInvalidWebXmlCheck("Marker file", "Invalid web.xml configuration ["
				+ XmlMarshallException.class.getSimpleName() + "]: Content is not allowed in prolog.", context);

		// Validate must have servlet
		this.undertakeInvalidWebXmlCheck("<web-app />", "Must have at least one servlet configured", context);

		// Validate must have servlet mapping
		this.undertakeInvalidWebXmlCheck("<web-app><servlet /></web-app>",
				"Must have at least one servlet-mapping configured", context);

		// Validate that reports valid with minimal configuration
		WebXmlSectionSource.validateWebXmlConfiguration(
				new ByteArrayInputStream("<web-app><servlet /><servlet-mapping /></web-app>".getBytes()), context);
	}

	/**
	 * Initiate.
	 * 
	 * @param webXmlContents
	 *            Contents of the <code>web.xml</code>.
	 * @param expectedReason
	 *            Expected reason for being invalid.
	 * @param context
	 *            {@link SourceContext}.
	 */
	private void undertakeInvalidWebXmlCheck(String webXmlContents, String expectedReason, SourceContext context) {
		try {
			WebXmlSectionSource.validateWebXmlConfiguration(new ByteArrayInputStream(webXmlContents.getBytes()),
					context);
			fail("Should not be successful");
		} catch (InvalidServletConfigurationException ex) {
			assertEquals("Incorrect reason", expectedReason, ex.getMessage());
		}
	}

	/**
	 * Ensure issue if no {@link Servlet} configured.
	 */
	public void testNoServlet() throws Exception {
		this.doDesignTest("NoServlet.xml", new DesignRecorder() {
			@Override
			public void record(SectionDesigner designer) {
				// Record no servlet configured
				this.recordInit();
				this.recordOfficeServletContext();
				this.recordRouteService();
				designer.addIssue("At least one <servlet/> element must be configured");
			}
		});
	}

	/**
	 * Ensure issue if no {@link Servlet} mapping configured.
	 */
	public void testNoServletMapping() throws Exception {
		this.doDesignTest("NoServletMapping.xml", new DesignRecorder() {
			@Override
			public void record(SectionDesigner designer) {
				// Record no servlet configured
				this.recordInit();
				this.recordOfficeServletContext();
				this.recordRouteService();
				designer.addIssue("At least one <servlet-mapping/> element must be configured");
				this.recordHttpServlet("Test", null);
			}
		});
	}

	/**
	 * Ensure can load a single {@link Servlet}.
	 */
	public void testSingleServlet() throws Exception {
		this.doDesignTest("SingleServlet.xml", new DesignRecorder() {
			@Override
			public void record(SectionDesigner designer) {
				// Record single servlet (with mapping)
				this.recordInit();
				this.recordOfficeServletContext();
				this.recordRouteService();
				this.recordHttpServlet("Test", "/", "one", "A", "two", "B");
			}
		});
	}

	/**
	 * Ensure can load a {@link Filter}.
	 */
	public void testFilter() {
		this.doDesignTest("Filter.xml", new DesignRecorder() {
			@Override
			void record(SectionDesigner designer) {
				// Record filter
				this.recordInit();
				this.recordFilter(new FilterInstance("Filter", MockFilter.class.getName(), "ONE", "a", "TWO", "b"));
				this.getFilterMappings().addFilterMapping("Filter", "/", null);
				this.recordOfficeServletContext();
				this.recordRouteService();
				this.recordHttpServlet("Servlet", "/");
			}
		});
	}

	/**
	 * Ensure can load a {@link Filter}.
	 */
	public void testComplexFilterMappings() {
		this.doDesignTest("ComplexFilterMappings.xml", new DesignRecorder() {
			@Override
			void record(SectionDesigner designer) {
				// Record filter
				this.recordInit();
				this.recordFilter(new FilterInstance("Filter", MockFilter.class.getName()));
				FilterMappings mappings = this.getFilterMappings();
				mappings.addFilterMapping("Filter", "/path/*", null);
				mappings.addFilterMapping("Filter", "*.extension", null);
				mappings.addFilterMapping("Filter", null, "Servlet", MappingType.REQUEST, MappingType.FORWARD,
						MappingType.INCLUDE);
				this.recordOfficeServletContext();
				this.recordRouteService();
				this.recordHttpServlet("Servlet", "/");
			}
		});
	}

	/**
	 * Ensure can configure MIME mappings.
	 */
	public void testMimeMappings() {
		this.doDesignTest("MimeMappings.xml", new DesignRecorder() {
			@Override
			void record(SectionDesigner designer) {
				// Record MIME mappings
				this.recordInit();
				this.recordMimeMapping("test", "plain/test");
				this.recordOfficeServletContext();
				this.recordRouteService();
				this.recordHttpServlet("Servlet", "/");
			}
		});
	}

	/**
	 * Ensure can configure {@link ServletContext} init parameters.
	 */
	public void testContextParams() {
		this.doDesignTest("ContextParams.xml", new DesignRecorder() {
			@Override
			void record(SectionDesigner designer) {
				// Record context params
				this.recordInit();
				this.recordOfficeServletContext("one", "A", "two", "B");
				this.recordRouteService();
				this.recordHttpServlet("Servlet", "/");
			}
		});
	}

}