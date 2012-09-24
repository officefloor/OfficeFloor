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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.objects.AutoWireObjectsRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.teams.AutoWireTeamsRepositoryImpl;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.objects.AutoWireObjectsLoader;
import net.officefloor.plugin.objects.AutoWireObjectsLoaderImpl;
import net.officefloor.plugin.teams.AutoWireTeamsLoader;
import net.officefloor.plugin.teams.AutoWireTeamsLoaderImpl;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireApplication;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

/**
 * <code>main</code> class to run a {@link WoofModel} on a
 * {@link HttpServerAutoWireApplication}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofOfficeFloorSource extends HttpServerAutoWireOfficeFloorSource {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(WoofOfficeFloorSource.class.getName());

	/**
	 * Property for the location of the WoOF configuration for the application.
	 */
	public static final String PROPERTY_WOOF_CONFIGURATION_LOCATION = "woof.configuration.location";

	/**
	 * Default WoOF configuration location.
	 */
	public static final String DEFAULT_WOOF_CONFIGUARTION_LOCATION = "application.woof";

	/**
	 * Property for the location of the Objects configuration for the
	 * application.
	 */
	public static final String PROPERTY_OBJECTS_CONFIGURATION_LOCATION = "objects.configuration.location";

	/**
	 * Default Objects configuration location.
	 */
	public static final String DEFAULT_OBJECTS_CONFIGURATION_LOCATION = "application.objects";

	/**
	 * Property for the location of the Teams configuration for the application.
	 */
	public static final String PROPERTY_TEAMS_CONFIGURATION_LOCATION = "teams.configuration.location";

	/**
	 * Default Teams configuration location.
	 */
	public static final String DEFAULT_TEAMS_CONFIGURATION_LOCATION = "application.teams";

	/**
	 * Path within Maven project to the <code>webapp</code> directory.
	 */
	public static final String WEBAPP_PATH = "src/main/webapp";

	/**
	 * Path within {@link #WEBAPP_PATH} for the <code>web.xml</code> file.
	 */
	public static final String WEBXML_FILE_PATH = "WEB-INF/web.xml";

	/**
	 * Property to specify the HTTP port.
	 */
	public static final String PROPERTY_HTTP_PORT = HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT;

	/**
	 * <p>
	 * Starts the {@link WoofOfficeFloorSource} for unit testing.
	 * <p>
	 * This will attempt to close all existing {@link OfficeFloor} instances to
	 * have a clean start.
	 * 
	 * @param args
	 *            Command line arguments which are paired name/values for the
	 *            {@link Property} loaded to the {@link OfficeFloorCompiler} for
	 *            compiling and running.
	 * @throws Exception
	 *             If fails to start.
	 */
	public static void start(String... args) throws Exception {

		// Stop all existing OfficeFloor instances
		stop();

		// Run the application
		main(args);
	}

	/**
	 * Stops the {@link WoofOfficeFloorSource} for unit testing.
	 * 
	 * @throws Exception
	 *             If fails to stop.
	 */
	public static void stop() throws Exception {
		// Stop all existing OfficeFloor instances
		AutoWireManagement.closeAllOfficeFloors();
	}

	/**
	 * <code>main</code> to run the {@link WoofModel}.
	 * 
	 * @param args
	 *            Command line arguments which are paired name/values for the
	 *            {@link Property} loaded to the {@link OfficeFloorCompiler} for
	 *            compiling and running.
	 * @throws Exception
	 *             If fails to run.
	 */
	public static void main(String... args) throws Exception {

		// Create the WoOF source
		WoofOfficeFloorSource source = new WoofOfficeFloorSource();

		// Configure properties from environment, system and command line
		OfficeFloorCompiler compiler = source.getOfficeFloorCompiler();
		for (int i = 0; i < args.length; i += 2) {

			// Obtain the name (stripping off possible leading '-')
			String name = args[i];
			if (name.startsWith("-")) {
				name = name.substring("-".length());
			}

			// Obtain the value (if available - ie not odd number of arguments)
			int valueIndex = i + 1;
			String value = (valueIndex >= args.length ? "" : args[valueIndex]);

			// Add the property value
			compiler.addProperty(name, value);
		}
		compiler.addSystemProperties();
		compiler.addEnvProperties();

		// Run WoOF
		run(source);
	}

	/**
	 * Configures and runs the {@link HttpServerAutoWireApplication}.
	 * 
	 * @param application
	 *            {@link WoofOfficeFloorSource}.
	 * @throws Exception
	 *             If fails to run.
	 */
	public static void run(WoofOfficeFloorSource application) throws Exception {

		// Maven project directory should be current directory
		File projectDir = new File(".");

		// Load the WoOF resources
		loadWebResourcesFromMavenProject(application.getOfficeFloorCompiler(),
				projectDir);

		// Start the application
		application.openOfficeFloor();
	}

	/**
	 * Loads the web application resources for WoOF within a Maven project.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param projectDirectory
	 *            Maven project directory.
	 */
	public static void loadWebResourcesFromMavenProject(
			OfficeFloorCompiler compiler, File projectDirectory) {

		// Determine if running within maven project
		if (!(new File(projectDirectory, "pom.xml").exists())) {
			LOGGER.warning("Not a Maven project as can not find pom.xml in "
					+ projectDirectory.getAbsolutePath());
			return; // must be a maven project
		}

		// Within maven project, so include webapp WoOF resources
		final File webAppDir = new File(projectDirectory, WEBAPP_PATH);
		if (!(webAppDir.exists())) {
			LOGGER.info(WEBAPP_PATH
					+ " not found and therefore not including. Typically should exist in project for web content.");
			return; // not include
		}

		// Load the web resources
		loadWebResources(compiler, webAppDir, new File[] { webAppDir });
	}

	/**
	 * Loads the web application resources for WoOF within a Maven project.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public static void loadWebResources(final OfficeFloorCompiler compiler,
			final File webAppDir, File[] resourceDirectories) {

		// Ensure have web app directory
		if (webAppDir == null) {
			LOGGER.warning("No web app directory provided so not including web resources");
			return; // must have web app directory
		}

		// Ensure the WEB-INF/web.xml file exists
		if (!(new File(webAppDir, WEBXML_FILE_PATH).exists())) {
			LOGGER.warning("Not including " + WEBAPP_PATH + " content as "
					+ WEBXML_FILE_PATH + " not found within "
					+ webAppDir.getAbsolutePath());
			return; // not include
		}

		// Configure resource directories
		SourceHttpResourceFactory.loadProperties(null, resourceDirectories,
				null, Boolean.FALSE, compiler);

		// Make WoOF resources available
		compiler.addResources(new ResourceSource() {
			@Override
			public InputStream sourceResource(String location) {

				// Determine if WoOF resource
				if (!(WoofUtil.isWoofResource(location))) {
					return null; // not WoOF resource
				}

				try {
					// Determine if within webapp directory
					File resource = new File(webAppDir, location);
					if (resource.exists()) {
						// Provide resource from webapp directory
						return new FileInputStream(resource);
					}
				} catch (IOException ex) {
					// Indicate unable to obtain resource
				}

				// Not found within webapp directory
				return null;
			}
		});
	}

	/**
	 * Loads extension functionality from the
	 * {@link WoofApplicationExtensionService} instances.
	 * 
	 * @param application
	 *            {@link WebAutoWireApplication}.
	 * @param properties
	 *            {@link SourceProperties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @throws Exception
	 *             If fails to load the extension functionality.
	 */
	public static void loadWebApplicationExtensions(
			WebAutoWireApplication application, SourceProperties properties,
			ClassLoader classLoader) throws Exception {

		// Load the application extensions
		ServiceLoader<WoofApplicationExtensionService> extensionServiceLoader = ServiceLoader
				.load(WoofApplicationExtensionService.class, classLoader);
		Iterator<WoofApplicationExtensionService> extensionIterator = extensionServiceLoader
				.iterator();
		while (extensionIterator.hasNext()) {
			try {

				// Obtain the next extension service
				WoofApplicationExtensionService extensionService = extensionIterator
						.next();

				// Extend the application
				extensionService
						.extendApplication(new WoofApplicationExtensionServiceContextImpl(
								application, properties, classLoader));

			} catch (ServiceConfigurationError ex) {
				// Warn that issue loading service
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(Level.WARNING, ex.getMessage(), ex);
				}

			} catch (Throwable ex) {
				// Warn that issue with service
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(
							Level.WARNING,
							WoofApplicationExtensionService.class
									.getSimpleName()
									+ " configuration failure: "
									+ ex.getMessage(), ex);
				}
			}
		}
	}

	/**
	 * Loads the optional configuration.
	 * 
	 * @param application
	 *            {@link AutoWireApplication}.
	 * @param objectsLocation
	 *            Location of the Objects {@link ConfigurationItem}.
	 * @param teamsLocation
	 *            Location of the Teams {@link ConfigurationItem}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @throws Exception
	 *             If fails to load the optional configuration.
	 */
	public static void loadOptionalConfiguration(
			AutoWireApplication application, String objectsLocation,
			String teamsLocation, ConfigurationContext configurationContext)
			throws Exception {

		// Load the optional objects configuration to the application
		ConfigurationItem objectsConfiguration = configurationContext
				.getConfigurationItem(objectsLocation);
		if (objectsConfiguration != null) {
			// Load the objects configuration
			AutoWireObjectsLoader objectsLoader = new AutoWireObjectsLoaderImpl(
					new AutoWireObjectsRepositoryImpl(new ModelRepositoryImpl()));
			objectsLoader.loadAutoWireObjectsConfiguration(
					objectsConfiguration, application);
		}

		// Load the optional teams configuration to the application
		ConfigurationItem teamsConfiguration = configurationContext
				.getConfigurationItem(teamsLocation);
		if (teamsConfiguration != null) {
			// Load the teams configuration
			AutoWireTeamsLoader teamsLoader = new AutoWireTeamsLoaderImpl(
					new AutoWireTeamsRepositoryImpl(new ModelRepositoryImpl()));
			teamsLoader.loadAutoWireTeamsConfiguration(teamsConfiguration,
					application);
		}
	}

	/**
	 * Allows overriding to provide additional configuration.
	 * 
	 * @param application
	 *            {@link HttpServerAutoWireOfficeFloorSource}.
	 */
	protected void configure(HttpServerAutoWireOfficeFloorSource application) {
		// No additional configuration by default
	}

	/*
	 * =================== AutoWireOfficeFloorSource ======================
	 */

	@Override
	protected void initOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Obtain the configuration context
		ClassLoader classLoader = this.getOfficeFloorCompiler()
				.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(
				classLoader);

		// Obtain the woof configuration (ensuring exists)
		String woofLocation = context.getProperty(
				PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);
		ConfigurationItem woofConfiguration = configurationContext
				.getConfigurationItem(woofLocation);
		if (woofConfiguration == null) {
			deployer.addIssue("Can not find WoOF configuration file '"
					+ woofLocation + "'", AssetType.OFFICE_FLOOR, "WoOF");
			return; // must have WoOF configuration
		}

		// Load the WoOF configuration to the application
		WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(
				new ModelRepositoryImpl()));
		woofLoader.loadWoofConfiguration(woofConfiguration, this);

		// Load the optional configuration to the application
		String objectsLocation = context.getProperty(
				PROPERTY_OBJECTS_CONFIGURATION_LOCATION,
				DEFAULT_OBJECTS_CONFIGURATION_LOCATION);
		String teamsLocation = context.getProperty(
				PROPERTY_TEAMS_CONFIGURATION_LOCATION,
				DEFAULT_TEAMS_CONFIGURATION_LOCATION);
		loadOptionalConfiguration(this, objectsLocation, teamsLocation,
				configurationContext);

		// Providing additional configuration
		this.configure(this);

		// Load extensions after configured WoOF
		loadWebApplicationExtensions(this, context, classLoader);

		// Initialise parent
		super.initOfficeFloor(deployer, context);
	}

	/**
	 * {@link WoofApplicationExtensionServiceContext} implementation.
	 */
	private static class WoofApplicationExtensionServiceContextImpl extends
			SourcePropertiesImpl implements
			WoofApplicationExtensionServiceContext {

		/**
		 * {@link WebAutoWireApplication}.
		 */
		private final WebAutoWireApplication application;

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * Initiate.
		 * 
		 * @param application
		 *            {@link WebAutoWireApplication}.
		 * @param properties
		 *            {@link SourceProperties}.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 */
		public WoofApplicationExtensionServiceContextImpl(
				WebAutoWireApplication application,
				SourceProperties properties, ClassLoader classLoader) {
			super(properties);
			this.application = application;
			this.classLoader = classLoader;
		}

		/*
		 * ============ WoofApplicationExtensionServiceContext =================
		 */

		@Override
		public WebAutoWireApplication getWebApplication() {
			return this.application;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

}