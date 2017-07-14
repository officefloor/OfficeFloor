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
package net.officefloor.plugin.woof;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.teams.WoofTeamsRepositoryImpl;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.objects.WoofObjectsLoader;
import net.officefloor.plugin.objects.WoofObjectsLoaderContext;
import net.officefloor.plugin.objects.WoofObjectsLoaderImpl;
import net.officefloor.plugin.teams.WoofTeamsLoader;
import net.officefloor.plugin.teams.WoofTeamsLoaderImpl;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireApplication;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * <code>main</code> class to run a {@link WoofModel} on a
 * {@link HttpServerAutoWireApplication}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofOfficeFloorSource {

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
	 * Property to override the default location of the <code>webapp</code>
	 * directory within a Maven project.
	 */
	public static final String PROPERTY_WEBAPP_LOCATION = "webapp.location";

	/**
	 * Default path within a Maven project to the <code>webapp</code> directory.
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
	 * WoOF {@link HttpTemplate} URI suffix.
	 */
	public static final String WOOF_TEMPLATE_URI_SUFFIX = ".woof";

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
		loadWebResourcesFromMavenProject(application, projectDir);

		// Start the application
		application.openOfficeFloor();
	}

	/**
	 * Loads the web application resources for WoOF within a Maven project.
	 * 
	 * @param contextConfigurable
	 *            {@link WoofContextConfigurable}.
	 * @param projectDirectory
	 *            Maven project directory.
	 */
	public static void loadWebResourcesFromMavenProject(WoofContextConfigurable contextConfigurable,
			File projectDirectory) {

		// Determine if running within maven project
		if (!(new File(projectDirectory, "pom.xml").exists())) {
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.warning("Not a Maven project as can not find pom.xml in " + projectDirectory.getAbsolutePath());
			}
			return; // must be a maven project
		}

		// Obtain the web app directory
		File webAppDir;
		String webAppLocation = System.getProperty(PROPERTY_WEBAPP_LOCATION);
		if (webAppLocation != null) {
			// Specified by system property
			webAppDir = new File(webAppLocation);

		} else {
			// Not configured so derive from default location
			webAppDir = new File(projectDirectory, WEBAPP_PATH);
		}

		// Within maven project, so include webapp WoOF resources
		if (!(webAppDir.exists())) {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info(webAppDir.getAbsolutePath()
						+ " not found and therefore not including. Typically should exist in project for web content.");
			}
			return; // not include
		}

		// Load the web resources
		loadWebResources(contextConfigurable, webAppDir, new File[] { webAppDir });
	}

	/**
	 * Loads the web application resources for WoOF within a Maven project.
	 * 
	 * @param contextConfigurable
	 *            {@link WoofContextConfigurable}.
	 * @param webAppDir
	 *            <code>webapp</code> directory.
	 * @param resourceDirectories
	 *            Directories to source public resources.
	 */
	public static void loadWebResources(final WoofContextConfigurable contextConfigurable, final File webAppDir,
			File... resourceDirectories) {

		// Ensure have web app directory
		if (webAppDir == null) {
			LOGGER.warning("No web app directory provided so not including web resources");
			return; // must have web app directory
		}

		// Ensure the WEB-INF/web.xml file exists
		if (!(new File(webAppDir, WEBXML_FILE_PATH).exists())) {
			LOGGER.warning("Not including webapp content as " + WEBXML_FILE_PATH + " not found within "
					+ webAppDir.getAbsolutePath());
			return; // not include
		}

		// Configure the webapp directory
		contextConfigurable.setWebAppDirectory(webAppDir);

		// Configure resource directories
		SourceHttpResourceFactory.loadProperties(null, resourceDirectories, null, Boolean.FALSE, contextConfigurable);

		// Make WoOF resources available
		contextConfigurable.addResources(new ResourceSource() {
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
	 *            {@link WebArchitect}.
	 * @param properties
	 *            {@link SourceProperties}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param resourceSources
	 *            {@link ResourceSource} instances.
	 * @throws Exception
	 *             If fails to load the extension functionality.
	 */
	public static void loadWebApplicationExtensions(WebArchitect application, SourceProperties properties,
			ClassLoader classLoader, ResourceSource... resourceSources) throws Exception {

		// Create the WoOF application extension context
		SourceContext sourceContext = new SourceContextImpl(false, classLoader, resourceSources);
		WoofApplicationExtensionServiceContext extensionContext = new WoofApplicationExtensionServiceContextImpl(
				application, sourceContext, properties);

		// Load the application extensions
		ServiceLoader<WoofApplicationExtensionService> extensionServiceLoader = ServiceLoader
				.load(WoofApplicationExtensionService.class, classLoader);
		Iterator<WoofApplicationExtensionService> extensionIterator = extensionServiceLoader.iterator();
		while (extensionIterator.hasNext()) {

			// Obtain the next extension service
			WoofApplicationExtensionService extensionService;
			try {
				extensionService = extensionIterator.next();
			} catch (ServiceConfigurationError ex) {
				// Warn that issue loading service
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(Level.WARNING, ex.getMessage(), ex);
				}

				// Not loaded, so continue onto next
				continue;
			}

			// Extend the application
			try {
				extensionService.extendApplication(extensionContext);

			} catch (Throwable ex) {
				// Warn that issue with service
				if (LOGGER.isLoggable(Level.WARNING)) {
					LOGGER.log(Level.WARNING, WoofApplicationExtensionService.class.getSimpleName() + " "
							+ extensionService.getClass().getName() + " configuration failure: " + ex.getMessage(), ex);
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
	 * @param deployer
	 *            {@link OfficeFloorDeployer}. May be <code>null</code>.
	 * @throws Exception
	 *             If fails to load the optional configuration.
	 */
	public static void loadOptionalConfiguration(final AutoWireApplication application, String objectsLocation,
			String teamsLocation, ConfigurationContext configurationContext, final OfficeFloorDeployer deployer)
			throws Exception {

		// Load the optional objects configuration to the application
		final ConfigurationItem objectsConfiguration = retrieveOptionalConfiguration(objectsLocation,
				configurationContext, DEFAULT_OBJECTS_CONFIGURATION_LOCATION);
		if (objectsConfiguration != null) {

			// Create the configuration context
			WoofObjectsLoaderContext context = new WoofObjectsLoaderContext() {

				@Override
				public ConfigurationItem getConfiguration() {
					return objectsConfiguration;
				}

				@Override
				public AutoWireApplication getOfficeArchitect() {
					return application;
				}

				@Override
				public void addIssue(String issueDescription) throws Exception {
					if (deployer == null) {
						// No deployer, so throw exception
						throw new Exception(issueDescription);

					} else {
						// Have deployer, so report the issue
						deployer.addIssue(issueDescription);
					}
				}
			};

			// Load the objects configuration
			WoofObjectsLoader objectsLoader = new WoofObjectsLoaderImpl(
					new WoofObjectsRepositoryImpl(new ModelRepositoryImpl()));
			objectsLoader.loadAutoWireObjectsConfiguration(context);
		}

		// Load the optional teams configuration to the application
		ConfigurationItem teamsConfiguration = retrieveOptionalConfiguration(teamsLocation, configurationContext,
				DEFAULT_TEAMS_CONFIGURATION_LOCATION);
		if (teamsConfiguration != null) {
			// Load the teams configuration
			WoofTeamsLoader teamsLoader = new WoofTeamsLoaderImpl(
					new WoofTeamsRepositoryImpl(new ModelRepositoryImpl()));
			teamsLoader.loadAutoWireTeamsConfiguration(teamsConfiguration, application);
		}
	}

	/**
	 * Retrieves the optional {@link ConfigurationItem}.
	 * 
	 * @param configurationLocation
	 *            Location of the {@link ConfigurationItem}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param defaultLocation
	 *            Default location of the {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem} or <code>null</code> if not able to
	 *         find.
	 * @throws Exception
	 *             If fails to retrieve the {@link ConfigurationItem}.
	 */
	private static ConfigurationItem retrieveOptionalConfiguration(String configurationLocation,
			ConfigurationContext configurationContext, String defaultLocation) throws Exception {

		// Retrieve the configuration
		ConfigurationItem configuration = configurationContext.getConfigurationItem(configurationLocation);
		if (configuration == null) {
			// Attempt to load via '.xml' suffix if default location
			if (defaultLocation.equals(configurationLocation)) {
				configuration = configurationContext.getConfigurationItem(defaultLocation + ".xml");
			}
		}

		// Return the configuration
		return configuration;
	}

	/**
	 * WoOF application {@link ResourceSource} instances.
	 */
	private final List<ResourceSource> applicationResourceSources = new LinkedList<ResourceSource>();

	/**
	 * Initiate.
	 */
	public WoofOfficeFloorSource() {
		// Provide default WoOF template suffix
		this.setDefaultHttpTemplateUriSuffix(WOOF_TEMPLATE_URI_SUFFIX);
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
	 * ======================= WoofContextConfigurable =========================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.getOfficeFloorCompiler().addProperty(name, value);
	}

	@Override
	public void setWebAppDirectory(final File webappDirectory) {

		// Include all webapp directory resources for application extension
		this.applicationResourceSources.add(new ResourceSource() {
			@Override
			public InputStream sourceResource(String location) {

				// Determine if resource exists
				File resource = new File(webappDirectory, location);
				if (!(resource.exists())) {
					return null; // resource not exist
				}

				// Return content of resource
				try {
					return new FileInputStream(resource);

				} catch (IOException ex) {

					// Log failure to source resource
					if (LOGGER.isLoggable(Level.WARNING)) {
						LOGGER.log(Level.WARNING, "Failed to source resource " + location + " from webapp directory "
								+ webappDirectory.getAbsolutePath(), ex);
					}

					// Failed to obtain content so no resource
					return null;
				}
			}
		});
	}

	@Override
	public void addResources(ResourceSource resourceSource) {

		// Configure application resources into compiler
		this.getOfficeFloorCompiler().addResources(resourceSource);

		// Include application resources for application extensions
		this.applicationResourceSources.add(resourceSource);
	}

	/*
	 * =================== AutoWireOfficeFloorSource ======================
	 */

	@Override
	protected void initOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {

		// Obtain the configuration context
		ClassLoader classLoader = this.getOfficeFloorCompiler().getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(classLoader);

		// Obtain the woof configuration (ensuring exists)
		String woofLocation = context.getProperty(PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);
		ConfigurationItem woofConfiguration = configurationContext.getConfigurationItem(woofLocation);
		if (woofConfiguration == null) {
			deployer.addIssue("Can not find WoOF configuration file '" + woofLocation + "'");
			return; // must have WoOF configuration
		}

		// Load the WoOF configuration to the application
		WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(new ModelRepositoryImpl()));
		woofLoader.loadWoofConfiguration(woofConfiguration, this, context);

		// Load the optional configuration to the application
		String objectsLocation = context.getProperty(PROPERTY_OBJECTS_CONFIGURATION_LOCATION,
				DEFAULT_OBJECTS_CONFIGURATION_LOCATION);
		String teamsLocation = context.getProperty(PROPERTY_TEAMS_CONFIGURATION_LOCATION,
				DEFAULT_TEAMS_CONFIGURATION_LOCATION);
		loadOptionalConfiguration(this, objectsLocation, teamsLocation, configurationContext, deployer);

		// Providing additional configuration
		this.configure(this);

		// Load extensions after configured WoOF
		ResourceSource[] resourceSources = this.applicationResourceSources
				.toArray(new ResourceSource[this.applicationResourceSources.size()]);
		loadWebApplicationExtensions(this, context, classLoader, resourceSources);

		// Initialise parent
		super.initOfficeFloor(deployer, context);
	}

	/**
	 * {@link WoofApplicationExtensionServiceContext} implementation.
	 */
	private static class WoofApplicationExtensionServiceContextImpl extends SourceContextImpl
			implements WoofApplicationExtensionServiceContext {

		/**
		 * {@link WebArchitect}.
		 */
		private final WebArchitect application;

		/**
		 * Initiate.
		 * 
		 * @param application
		 *            {@link WebArchitect}.
		 * @param sourceContext
		 *            {@link SourceContext}.
		 * @param properties
		 *            {@link SourceProperties}.
		 */
		public WoofApplicationExtensionServiceContextImpl(WebArchitect application, SourceContext sourceContext,
				SourceProperties properties) {
			super(false, sourceContext, properties);
			this.application = application;
		}

		/*
		 * ============ WoofApplicationExtensionServiceContext =================
		 */

		@Override
		public WebArchitect getWebApplication() {
			return this.application;
		}
	}

}