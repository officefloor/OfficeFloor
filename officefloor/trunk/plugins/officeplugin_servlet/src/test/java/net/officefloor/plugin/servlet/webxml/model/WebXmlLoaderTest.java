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
package net.officefloor.plugin.servlet.webxml.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link WebXmlLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebXmlLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct configuration loaded.
	 */
	public void testLoadConfiguration() throws Exception {

		final SectionSourceContext context = this
				.createMock(SectionSourceContext.class);

		// Obtain location of web.xml file
		File webXmlFile = this.findFile(this.getClass(), "web.xml");
		File unmarshalFile = this.findFile(this.getClass(),
				"UnmarshalWebXml.xml");

		// Record loading configuration
		this.recordReturn(context, context.getResource("WEB-INF/web.xml"),
				new FileInputStream(webXmlFile));
		this.recordReturn(
				context,
				context.getResource("net/officefloor/plugin/servlet/webxml/model/UnmarshalWebXml.xml"),
				new FileInputStream(unmarshalFile));

		// Load the configuration
		this.replayMockObjects();
		WebXmlLoader loader = new WebXmlLoader();
		InputStream webXmlConfiguration = context
				.getResource("WEB-INF/web.xml");
		WebAppModel webApp = loader.loadConfiguration(webXmlConfiguration,
				context);
		this.verifyMockObjects();

		// Validate the web-app
		assertEquals("Incorrect version", "2.5", webApp.getVersion());
		assertEquals("Incorrect diplay-name", "Test", webApp.getDisplayName());

		// Validate the context-param
		assertEquals("Incorrect number of context-param's", 2, webApp
				.getContextParams().size());
		assertContextParam(webApp.getContextParams().get(0), "one", "A");
		assertContextParam(webApp.getContextParams().get(1), "two", "B");

		// Validate the mime-mapping
		assertEquals("Incorrect number of mime-mapping's", 2, webApp
				.getMimeMappings().size());
		assertMimeMapping(webApp.getMimeMappings().get(0), "txt", "text/plain");
		assertMimeMapping(webApp.getMimeMappings().get(1), "html", "text/html");

		// Validate the servlet
		assertEquals("Incorrect number of servlet's", 2, webApp.getServlets()
				.size());
		assertServlet(webApp.getServlets().get(0), "Servlet1",
				"net.officefloor.ExampleServlet", "a", "ONE", "b", "TWO");
		assertServlet(webApp.getServlets().get(1), "Servlet2",
				"net.officefloor.AnotherServlet");

		// Validate the servlet-mapping
		assertEquals("Incorrect number of servlet-mapping's", 2, webApp
				.getServletMappings().size());
		assertServletMapping(webApp.getServletMappings().get(0), "Servlet1",
				"/path/*", "*.extension");
		assertServletMapping(webApp.getServletMappings().get(1), "Servlet2",
				"/exact/path");

		// Validate the filter
		assertEquals("Incorrect number of filter's", 2, webApp.getFilters()
				.size());
		assertFilter(webApp.getFilters().get(0), "Filter1",
				"net.officefloor.ExampleFilter", "x", "m", "y", "n");
		assertFilter(webApp.getFilters().get(1), "Filter2",
				"net.officefloor.AnotherFilter");

		// Validate the filter-mapping
		assertEquals("Incorrect number of filter-mapping's", 2, webApp
				.getFilterMappings().size());
		assertFilterMapping(webApp.getFilterMappings().get(0), "Filter1",
				new String[] { "/exact/path", "/path/*", "*.extension" }, null,
				"REQUEST", "FORWARD", "INCLUDE");
		assertFilterMapping(webApp.getFilterMappings().get(1), "Filter2", null,
				new String[] { "Servlet1", "Servlet2" });
	}

	/**
	 * Asserts the {@link ContextParamModel}.
	 * 
	 * @param param
	 *            {@link ContextParamModel} to validate.
	 * @param name
	 *            Expected name.
	 * @param value
	 *            Expected value.
	 */
	private static void assertContextParam(ContextParamModel param,
			String name, String value) {
		assertEquals("Incorrect param-name", name, param.getName());
		assertEquals("Incorrect param-value", value, param.getValue());
	}

	/**
	 * Asserts the {@link MimeMappingModel}.
	 * 
	 * @param mapping
	 *            {@link MimeMappingModel} to validate.
	 * @param extension
	 *            Expected extension.
	 * @param mimeType
	 *            Expected MIME type.
	 */
	private static void assertMimeMapping(MimeMappingModel mapping,
			String extension, String mimeType) {
		assertEquals("Incorrect extension", extension, mapping.getExtension());
		assertEquals("Incorrect mime-type", mimeType, mapping.getMimeType());
	}

	/**
	 * Asserts the {@link ServletModel}.
	 * 
	 * @param servlet
	 *            {@link ServletModel} to validate.
	 * @param servletName
	 *            Expected {@link Servlet} name.
	 * @param servletClass
	 *            Expected class name.
	 * @param initParamNameValues
	 *            Expected init param name value pairs.
	 */
	private static void assertServlet(ServletModel servlet, String servletName,
			String servletClass, String... initParamNameValues) {
		assertEquals("Incorrect servlet-name", servletName,
				servlet.getServletName());
		assertEquals("Incorrect servlet-class", servletClass,
				servlet.getServletClass());
		assertEquals("Incorrect number of init-param's",
				(initParamNameValues.length / 2), servlet.getInitParams()
						.size());
		for (int i = 0; i < initParamNameValues.length; i += 2) {
			String expectedName = initParamNameValues[i];
			String expectedValue = initParamNameValues[i + 1];
			int index = (i / 2);
			InitParamModel initParam = servlet.getInitParams().get(index);
			assertEquals("Incorrect param-name for index " + index,
					expectedName, initParam.getName());
			assertEquals("Incorrect param-value for index " + index,
					expectedValue, initParam.getValue());
		}
	}

	/**
	 * Asserts the {@link ServletMappingModel}.
	 * 
	 * @param mapping
	 *            {@link ServletMappingModel} to be validated.
	 * @param servletName
	 *            Expected {@link Servlet} name.
	 * @param urlPatterns
	 *            Expected URL patterns.
	 */
	private static void assertServletMapping(ServletMappingModel mapping,
			String servletName, String... urlPatterns) {
		assertEquals("Incorrect servlet-name", servletName,
				mapping.getServletName());
		assertEquals("Incorrect number of url-pattern's", urlPatterns.length,
				mapping.getUrlPatterns().size());
		for (int i = 0; i < urlPatterns.length; i++) {
			assertEquals("Incorrect url-pattern for index " + i,
					urlPatterns[i], mapping.getUrlPatterns().get(i));
		}
	}

	/**
	 * Asserts the {@link FilterModel}.
	 * 
	 * @param filter
	 *            {@link FilterModel} to be validate.
	 * @param filterName
	 *            Expected {@link Filter} name.
	 * @param filterClass
	 *            Expected {@link Filter} class.
	 * @param initParamNameValues
	 *            Expected init param name value pairs.
	 */
	private static void assertFilter(FilterModel filter, String filterName,
			String filterClass, String... initParamNameValues) {
		assertEquals("Incorrect filter-name", filterName,
				filter.getFilterName());
		assertEquals("Incorrect filter-class", filterClass,
				filter.getFilterClass());
		assertEquals("Incorrect number of init-param's",
				(initParamNameValues.length / 2), filter.getInitParams().size());
		for (int i = 0; i < initParamNameValues.length; i += 2) {
			String expectedName = initParamNameValues[i];
			String expectedValue = initParamNameValues[i + 1];
			int index = (i / 2);
			InitParamModel initParam = filter.getInitParams().get(index);
			assertEquals("Incorrect param-name for index " + index,
					expectedName, initParam.getName());
			assertEquals("Incorrect param-value for index " + index,
					expectedValue, initParam.getValue());
		}
	}

	/**
	 * Asserts the {@link FilterMappingModel}.
	 * 
	 * @param mapping
	 *            {@link FilterMappingModel} to validate.
	 * @param filterName
	 *            Expected {@link Filter} name.
	 * @param urlPatterns
	 * @param servletNames
	 * @param dispatchers
	 */
	private static void assertFilterMapping(FilterMappingModel mapping,
			String filterName, String[] urlPatterns, String[] servletNames,
			String... dispatchers) {
		assertEquals("Incorrect filter-name", filterName,
				mapping.getFilterName());
		if (urlPatterns == null) {
			assertEquals("Expecting no url-pattern's", 0, mapping
					.getUrlPatterns().size());
		} else {
			assertEquals("Incorrect number of url-pattern's",
					urlPatterns.length, mapping.getUrlPatterns().size());
			for (int i = 0; i < urlPatterns.length; i++) {
				assertEquals("Incorrect url-pattern at index " + i,
						urlPatterns[i], mapping.getUrlPatterns().get(i));
			}
		}
		if (servletNames == null) {
			assertEquals("Expecting no servlet-name's", 0, mapping
					.getServletNames().size());
		} else {
			assertEquals("Incorrect number of servlet-name's",
					servletNames.length, mapping.getServletNames().size());
			for (int i = 0; i < servletNames.length; i++) {
				assertEquals("Incorrect servlet-name at index " + i,
						servletNames[i], mapping.getServletNames().get(i));
			}
		}
		assertEquals("Incorrect number of dispatcher's", dispatchers.length,
				mapping.getDispatchers().size());
		for (int i = 0; i < dispatchers.length; i++) {
			assertEquals("Incorrect dispatcher at index " + i, dispatchers[i],
					mapping.getDispatchers().get(i));
		}
	}

}