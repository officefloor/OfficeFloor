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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Launcher of WoOF in development mode.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentLauncher {

	/**
	 * Allows specifying the GWT main class.
	 */
	public static final String GWT_MAIN_CLASS_NAME = "gwt.main.class.name";

	/**
	 * Name of the default GWT development main class to use.
	 */
	public static final String DEFAULT_GWT_MAIN_CLASS_NAME = "com.google.gwt.dev.DevMode";

	/**
	 * Name of the GWT Module to use should the application not be using GWT.
	 */
	public static final String NO_GWT_MODULE_NAME = "net.officefloor.launch.NoGwt";

	/**
	 * {@link GwtLauncher} to use to launch GWT.
	 */
	private static GwtLauncher launcher = null;

	/**
	 * <p>
	 * Specifies the {@link GwtLauncher}.
	 * <p>
	 * Typically the default {@link GwtLauncher} should be used. This is to
	 * allow testing to override to avoid {@link System#exit(int)} within the
	 * GWT development mode code.
	 * 
	 * @param launcher
	 *            {@link GwtLauncher}.
	 */
	public static void setGwtLauncher(GwtLauncher launcher) {
		WoofDevelopmentLauncher.launcher = launcher;
	}

	/**
	 * Default {@link GwtLauncher}.
	 */
	private static class DefaultGwtLauncher implements GwtLauncher {

		/*
		 * ===================== GwtLauncher ===========================
		 */

		@Override
		public void launch(String... arguments) throws Exception {

			// Obtain the name of the GWT main class
			String mainClassName = System.getProperty(GWT_MAIN_CLASS_NAME,
					DEFAULT_GWT_MAIN_CLASS_NAME);

			// Load and reflectively invoke so as not to require import
			Class<?> mainClass = Thread.currentThread().getContextClassLoader()
					.loadClass(mainClassName);
			Method mainMethod = mainClass.getMethod("main",
					new String[0].getClass());
			mainMethod.invoke(null, (Object) arguments);
		}
	}

	/**
	 * <code>main</code> method to launch WoOF.
	 * 
	 * @param arguments
	 *            The first argument must be the location of the
	 *            {@link WoofDevelopmentConfiguration} file. The remaining
	 *            arguments are passed as is to the {@link GwtLauncher}.
	 * @throws Exception
	 *             If fails to launch WoOF.
	 */
	public static void main(String... arguments) throws Exception {

		// Obtain the configuration file
		File configurationFile = new File(arguments[0]);

		// Load the configuration
		WoofDevelopmentConfiguration configuration = new WoofDevelopmentConfiguration(
				configurationFile);

		// Create the arguments
		List<String> gwtArguments = new LinkedList<String>();

		// Configure WoOF embedded server
		gwtArguments.addAll(Arrays.asList("-server",
				WoofServletContainerLauncher.class.getName()));

		// Configure the war as configuration file
		File warDirectory = configuration.getWarDirectory();
		gwtArguments.addAll(Arrays.asList("-war",
				warDirectory.getAbsolutePath()));

		// Add Startup URLs
		for (String startupUrl : configuration.getStartupUrls()) {
			gwtArguments.addAll(Arrays.asList("-startupUrl", startupUrl));
		}

		// Add GWT module names
		String[] moduleNames = configuration.getModuleNames();
		if (moduleNames.length == 0) {
			// Use the no GWT module
			gwtArguments.add(NO_GWT_MODULE_NAME);

		} else {
			// Load the GWT modules
			for (String moduleName : moduleNames) {
				gwtArguments.add(moduleName);
			}
		}

		// Obtain the GWT Launcher
		GwtLauncher gwtLauncher = launcher;
		if (gwtLauncher == null) {
			gwtLauncher = new DefaultGwtLauncher();
		}

		// Launch GWT
		gwtLauncher
				.launch(gwtArguments.toArray(new String[gwtArguments.size()]));
	}

}