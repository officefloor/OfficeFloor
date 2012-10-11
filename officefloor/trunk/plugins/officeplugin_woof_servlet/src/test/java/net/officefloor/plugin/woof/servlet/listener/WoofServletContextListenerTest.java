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
package net.officefloor.plugin.woof.servlet.listener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.FilteredServlet;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.plugin.woof.servlet.WoofServletContextListener;
import net.officefloor.plugin.woof.servlet.WoofServletFilter;

import org.easymock.AbstractMatcher;

/**
 * <p>
 * Tests the {@link WoofServletContextListener}.
 * <p>
 * Note the <code>web-fragment</code> configuration is tested by the tutorials.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletContextListenerTest extends OfficeFrameTestCase {

	/**
	 * {@link WoofServletContextListener} to test.
	 */
	private final WoofServletContextListener listener = new WoofServletContextListener();

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
	 * Expected location of the WoOF application configuration.
	 */
	private String expectedApplicationWoofLocation = WoofOfficeFloorSource.DEFAULT_WOOF_CONFIGUARTION_LOCATION;

	/**
	 * WoOF application configuration. Should not be read by
	 * {@link WoofServletContextListener}.
	 */
	private InputStream woofApplicationConfiguration = new ByteArrayInputStream(
			new byte[0]);

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader = new ClassLoader() {
		@Override
		public InputStream getResourceAsStream(String name) {

			// Ensure correct location being requested
			assertEquals(
					"Incorrect WoOF application location requested",
					WoofServletContextListenerTest.this.expectedApplicationWoofLocation,
					name);

			// Provide the WoOF application configuration
			return WoofServletContextListenerTest.this.woofApplicationConfiguration;
		}
	};

	/**
	 * {@link Thread} context {@link ClassLoader}.
	 */
	private ClassLoader threadContextClassLoader;

	/**
	 * Ensure not load {@link WoofServletContextListener} if no WoOF
	 * configuration file.
	 */
	public void testNoApplicationWoofFile() {

		// No woof configuration
		this.woofApplicationConfiguration = null;

		this.recordInitParameter(null);
		this.doTest();
	}

	/**
	 * Ensure not load {@link WoofServletContextListener} if no WoOF
	 * configuration file at specified location.
	 */
	public void testNoApplicationWoofFileAtSpecifiedLocation() {

		// No woof configuration
		this.woofApplicationConfiguration = null;

		this.recordInitParameter("another/location/file.woof");
		this.doTest();
	}

	/**
	 * Ensure not add {@link WoofServletFilter} if already registered.
	 */
	public void testFilterAlreadyRegistered() {

		final FilterRegistration filterRegistrion = this
				.createMock(FilterRegistration.class);

		// Record WoOF filter already registered
		this.recordInitParameter(null);
		this.recordReturn(this.context, this.context
				.getFilterRegistration(WoofServletFilter.FILTER_NAME),
				filterRegistrion);

		// Test
		this.doTest();
	}

	/**
	 * Ensure automatically register the {@link WoofServletFilter} if not yet
	 * registered and there is an <code>application.woof</code> file.
	 */
	public void testAutomaticallyRegisterFilter() throws IOException {

		final javax.servlet.ServletRegistration.Dynamic servletDynamic = this
				.createMock(javax.servlet.ServletRegistration.Dynamic.class);
		final Dynamic filterDynamic = this.createMock(Dynamic.class);

		// Obtain the WoOF template configurations
		File gwtFile = this.findFile("src/test/webapp/gwt.woof.ofp");
		File templateFile = this.findFile("src/test/webapp/Template.woof.ofp");

		// Reinstate class loader (to enable loading WoOF configuration)
		Thread.currentThread().setContextClassLoader(
				this.threadContextClassLoader);

		// Record determining that WoOF Servlet Filter not registered
		this.recordInitParameter(null);
		this.recordReturn(this.context, this.context
				.getFilterRegistration(WoofServletFilter.FILTER_NAME), null);

		// Record initialising the WoOF Servlet Filter
		this.recordReturn(this.context, this.context.getContextPath(), "/");
		this.recordReturn(
				this.context,
				this.context
						.getInitParameter(WoofOfficeFloorSource.PROPERTY_WOOF_CONFIGURATION_LOCATION),
				null);
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
		this.recordReturn(this.context, this.context.addServlet(
				"OfficeFloorFilteredServlet", FilteredServlet.class),
				servletDynamic);
		this.recordReturn(servletDynamic, servletDynamic.addMapping(
				"/gwt/comet-subscribe", "/test", "/gwt/comet-publish", "/gwt",
				"/gwt/service", "*.task"), new HashSet<String>(),
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						String[] expectedUrls = (String[]) expected[0];
						String[] actualUrls = (String[]) actual[0];
						assertEquals("Incorrect number of URLs",
								expectedUrls.length, actualUrls.length);
						for (int i = 0; i < expectedUrls.length; i++) {
							assertEquals("Incorret URL " + i, expectedUrls[i],
									actualUrls[i]);
						}
						return true;
					}
				});
		// Load type
		this.recordReturn(this.context,
				this.context.getResourceAsStream("/gwt.woof.ofp"),
				new FileInputStream(gwtFile));
		this.recordReturn(this.context,
				this.context.getResourceAsStream("/Template.woof.ofp"),
				new FileInputStream(templateFile));
		// Load for use
		this.recordReturn(this.context,
				this.context.getResourceAsStream("/gwt.woof.ofp"),
				new FileInputStream(gwtFile));
		this.recordReturn(this.context,
				this.context.getResourceAsStream("/Template.woof.ofp"),
				new FileInputStream(templateFile));

		// Record adding the WoOF Servlet Filter
		this.recordReturn(this.context, this.context.addFilter(
				WoofServletFilter.FILTER_NAME, new WoofServletFilter()),
				filterDynamic, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect filer name", expected[0],
								actual[0]);
						assertTrue("Must be WoofServletFilter instance",
								(actual[1].getClass()
										.equals(WoofServletFilter.class)));
						return true;
					}
				});
		filterDynamic.addMappingForUrlPatterns(null, false, "/*");
		this.control(filterDynamic).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertNull("Incorrect dispatch types", actual[0]);
				assertEquals("Incorrect before/after flag", expected[1],
						actual[1]);
				String[] expectedUrls = (String[]) expected[2];
				String[] actualUrls = (String[]) actual[2];
				assertEquals("Incorrect number of URLs", expectedUrls.length,
						actualUrls.length);
				assertEquals("Incorrect URL", expectedUrls[0], actualUrls[0]);
				return true;
			}
		});

		// Test
		this.doTest();
	}

	/*
	 * ======================== Helper methods ===============================
	 */

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

		// Do nothing further if null as defaults already specified
		if (woofConfigurationLocation == null) {
			return;
		}

		// Provide alternate location to obtain WoOF configuration
		this.expectedApplicationWoofLocation = woofConfigurationLocation;
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

		// Override the thread class loader
		Thread currentThread = Thread.currentThread();
		this.threadContextClassLoader = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(this.classLoader);
	}

	@Override
	protected void tearDown() throws Exception {

		// Reinstate the thread class loader
		Thread.currentThread().setContextClassLoader(
				this.threadContextClassLoader);
	}

}