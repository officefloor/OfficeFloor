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
package net.officefloor.launch.woof;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Ignore;

import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import com.google.gwt.dev.DevMode;

/**
 * Tests the {@link WoofDevelopmentLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO complete unit tests and fix to pass")
public class WoofDevelopmentLauncherTest extends OfficeFrameTestCase implements
		GwtLauncher {

	/**
	 * {@link MockDevMode}.
	 */
	private MockDevMode devMode;

	@Override
	protected void setUp() throws Exception {
		// Set test as launcher to avoid System.exit(int) calls
		WoofDevelopmentLauncher.setGwtLauncher(this);
	}

	@Override
	protected void tearDown() throws Exception {
		// Clear launcher
		WoofDevelopmentLauncher.setGwtLauncher(null);

		// Stop GWT
		if (this.devMode != null) {
			this.devMode.onDone();
		}
	}

	/**
	 * Ensure correct {@link DevMode} class name.
	 */
	public void testCorrectDevModeClassName() {
		assertEquals("Incorrect GWT main class", DevMode.class.getName(),
				WoofDevelopmentLauncher.DEFAULT_GWT_MAIN_CLASS_NAME);
	}

	/**
	 * Ensure given the project <code>pom.xml</code> that able to determine the
	 * appropriate class path for GWT {@link DevMode}.
	 */
	public void testGwtClassPath() {
		
		// Load the class path entries
		
		
		fail("TODO implement");
	}

	/**
	 * Ensure the application runs so remaining test focus on GWT
	 * {@link DevMode} integration.
	 */
	public void testEnsureApplicationRuns() throws Exception {
		try {
			// Run application
			WoofOfficeFloorSource.main();

		} finally {
			// Ensure stop
			AutoWireManagement.closeAllOfficeFloors();
		}
	}

	/**
	 * Ensure can start the embedded WoOF server within {@link DevMode}.
	 */
	public void testEmbeddedWoofServer() throws Exception {

		// Obtain the project directory
		File projectDirectory = this.findFile("pom.xml").getParentFile();

		// Re-use test class path for running

		// Create configuration for running (keeps command smaller)
		File woofFile = this.findFile("application.woof");
		WoofDevelopmentConfiguration configuration = WoofDevelopmentConfigurationLoader
				.loadConfiguration(new FileInputStream(woofFile));
		File configurationFile = File.createTempFile("woof-configuration-",
				".properties");
		configuration.storeConfiguration(configurationFile);

		// Run the dev mode
		WoofDevelopmentLauncher.main(projectDirectory.getAbsolutePath());

		// TODO remove
		System.out.print("Press enter to continue");
		System.out.flush();
		System.in.read();
		
		// TODO validate the arguments

		// TODO test invoke
		throw new UnsupportedOperationException(
				"TODO ensure can call embedded server");
	}

	/*
	 * =================== GwtLauncher ============================
	 */

	@Override
	public void launch(String... arguments) throws Exception {

		/*
		 * Runs the {@link DevMode} in similar way to its main method except
		 * without System.exit(int) calls to disrupt unit testing.
		 */
		this.devMode = new MockDevMode();
		this.devMode.mockMain(arguments);
	}

	/**
	 * Mock {@link DevMode} to provide access to necessary class members for
	 * unit testing.
	 */
	private class MockDevMode extends DevMode {

		/**
		 * Mock main for testing.
		 * 
		 * @param arguments
		 *            Arguments.
		 */
		public void mockMain(String[] arguments) {

			// By default run head less (for continuous integration)
			boolean isHeadless = Boolean.parseBoolean(System.getProperty(
					"hide.gwt.devmode", String.valueOf(true)));
			this.setHeadless(isHeadless);

			// Process arguments and if successful run
			if (new ArgProcessor(this.options).processArgs(arguments)) {
				// Run in new thread as blocks until complete
				new Thread() {
					@Override
					public void run() {
						MockDevMode.this.run();
					}
				}.start();
			}
		}
	}

}