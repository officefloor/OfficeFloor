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
package net.officefloor.plugin.woof.servlet.configure;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.ServletWebAutoWireApplication;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.plugin.woof.servlet.WoofServlet;

import org.easymock.AbstractMatcher;

/**
 * <p>
 * Tests the configuring of the {@link WoofServlet}.
 * <p>
 * Note the <code>web-fragment</code> configuration is tested by the tutorials.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletConfigureTest extends OfficeFrameTestCase {

	/**
	 * {@link WoofServlet} to test.
	 */
	private final WoofServlet listener = new WoofServlet();

	/**
	 * Mock {@link ServletContext}.
	 */
	private final ServletContext context = this
			.createMock(ServletContext.class);

	/**
	 * Mock {@link ServletContextEvent}.
	 */
	private final ServletContextEvent event = new ServletContextEvent(
			this.context);

	/**
	 * Ensure not add {@link WoofServlet} if already registered.
	 */
	public void testServletAlreadyRegistered() {

		final ServletRegistration servletRegistrion = this
				.createMock(ServletRegistration.class);

		// Record WoOF servlet already registered
		this.recordReturn(this.context,
				this.context.getServletRegistration(WoofServlet.SERVLET_NAME),
				servletRegistrion);
		this.context.log("Not registering OfficeFloorServlet WoOF ("
				+ WoofServlet.class.getName()
				+ ") as Servlet already registered under name");

		// Test
		this.doTest();
	}

	/**
	 * Ensure not load {@link WoofServletContextListener} if no WoOF
	 * configuration file.
	 */
	public void testNoApplicationWoofFile() {
		this.recordNotAlreadyRegistered();
		this.recordInitParameter("another/location/file.woof");
		this.context
				.log("No WoOF configuration file at location another/location/file.woof."
						+ " WoOF functionality will not be configured.");
		this.doTest();
	}

	/**
	 * Ensure automatically register the {@link WoofServlet} if not yet
	 * registered and there is an <code>application.woof</code> file.
	 */
	public void testAutomaticallyRegisterServlet() throws IOException {

		final Dynamic servletDynamic = this.createMock(Dynamic.class);
		final javax.servlet.FilterRegistration.Dynamic filterDynamic = this
				.createMock(javax.servlet.FilterRegistration.Dynamic.class);

		// Record determining that WoOF Servlet not registered
		this.recordNotAlreadyRegistered();
		this.recordInitParameter(null);

		// Record initialising the WoOF Servlet
		this.recordReturn(
				this.context,
				this.context
						.getInitParameter(WoofOfficeFloorSource.PROPERTY_OBJECTS_CONFIGURATION_LOCATION),
				null);
		this.recordReturn(
				this.context,
				this.context
						.getInitParameter(WoofOfficeFloorSource.PROPERTY_TEAMS_CONFIGURATION_LOCATION),
				null);

		// Configure the WoOF Servlet
		this.recordReturn(this.context, this.context.addServlet(
				WoofServlet.SERVLET_NAME, WoofServlet.class), servletDynamic);
		this.recordReturn(servletDynamic, servletDynamic.setInitParameter(
				"officefloorservlet.application.index", "1"), true);
		this.recordReturn(servletDynamic, servletDynamic.addMapping(
				"/gwt/comet-subscribe", "/gwt/comet-publish", "/gwt/service",
				"*.woof"), new HashSet<String>(), new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				String[] expectedUrls = (String[]) expected[0];
				String[] actualUrls = (String[]) actual[0];
				assertEquals("Incorrect number of URLs", expectedUrls.length,
						actualUrls.length);
				for (int i = 0; i < expectedUrls.length; i++) {
					assertEquals("Incorret URL " + i, expectedUrls[i],
							actualUrls[i]);
				}
				return true;
			}
		});
		servletDynamic.setLoadOnStartup(1);

		// Configure the Filter
		this.recordReturn(this.context, this.context.addFilter(
				WoofServlet.SERVLET_NAME, WoofServlet.class), filterDynamic);
		this.recordReturn(filterDynamic, filterDynamic.setInitParameter(
				"officefloorservlet.application.index", "1"), true);
		filterDynamic.addMappingForUrlPatterns(null, false,
				"/gwt/comet-subscribe", "/gwt/comet-publish", "/gwt/service",
				"*.woof");
		this.control(filterDynamic).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertNull("Should be default dispatching", actual[0]);
				assertFalse("Should be loaded before other filters",
						(Boolean) actual[1]);
				String[] expectedUrls = (String[]) expected[2];
				String[] actualUrls = (String[]) actual[2];
				assertEquals("Incorrect number of URLs", expectedUrls.length,
						actualUrls.length);
				for (int i = 0; i < expectedUrls.length; i++) {
					assertEquals("Incorret URL " + i, expectedUrls[i],
							actualUrls[i]);
				}
				return true;
			}
		});

		// Log configuration
		this.context
				.log("WoOF Servlet/Filter ("
						+ WoofServlet.class.getName()
						+ ") loaded to service "
						+ "/gwt/comet-subscribe, /gwt/comet-publish, /gwt/service, *.woof");

		// Test
		this.doTest();
	}

	/*
	 * ======================== Helper methods ===============================
	 */

	/**
	 * Records the {@link WoofServlet} to not already be registered.
	 */
	private void recordNotAlreadyRegistered() {
		this.recordReturn(this.context,
				this.context.getServletRegistration(WoofServlet.SERVLET_NAME),
				null);
		this.recordReturn(this.context, this.context.getContextPath(), "/");
	}

	/**
	 * Record the WoOF application configuration location.
	 * 
	 * @param woofConfigurationLocation
	 *            Location of the WoOF application configuration.
	 *            <code>null</code> to use default location.
	 */
	private void recordInitParameter(String woofConfigurationLocation) {

		// Record obtaining the location
		this.recordReturn(
				this.context,
				this.context
						.getInitParameter(WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION),
				woofConfigurationLocation);
	}

	/**
	 * Undertakes the test.
	 */
	private void doTest() {
		this.replayMockObjects();
		this.listener.contextInitialized(this.event);
		this.verifyMockObjects();
	}

	@Override
	protected void setUp() throws Exception {

		// Reset OfficeFloorServlet Application
		ServletWebAutoWireApplication.reset();
	}

}