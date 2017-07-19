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
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.classloader.ClassLoaderConfigurationContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.model.teams.WoofTeamsRepositoryImpl;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.plugin.objects.WoofObjectsLoader;
import net.officefloor.plugin.objects.WoofObjectsLoaderContext;
import net.officefloor.plugin.objects.WoofObjectsLoaderImpl;
import net.officefloor.plugin.teams.WoofTeamsLoader;
import net.officefloor.plugin.teams.WoofTeamsLoaderContext;
import net.officefloor.plugin.teams.WoofTeamsLoaderImpl;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.application.WebArchitectEmployer;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireApplication;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * {@link OfficeFloorExtensionService} / {@link OfficeExtensionService} to
 * configure the {@link WoofModel} into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofOfficeExtensionService implements OfficeFloorExtensionService, OfficeExtensionService {

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
	 * Default path within a Maven project to the <code>webapp</code> directory.
	 */
	public static final String WEBAPP_PATH = "src/main/webapp";

	/**
	 * WoOF {@link HttpTemplate} URI suffix.
	 */
	public static final String WOOF_TEMPLATE_URI_SUFFIX = ".woof";

	/**
	 * Loads the web application resources for WoOF within a Maven project.
	 * 
	 * @param context
	 *            {@link OfficeFloorExtensionContext.
	 * @param projectDirectory
	 *            Maven project directory.
	 */
	public static void loadWebResourcesFromMavenProject(OfficeFloorExtensionContext context, File projectDirectory) {

		// Determine if running within maven project
		if (!(new File(projectDirectory, "pom.xml").exists())) {
			return; // must be a maven project
		}

		// Obtain the web app directory
		File webAppDir;
		String webAppLocation = context.getProperty(PROPERTY_WEBAPP_LOCATION);
		if (webAppLocation != null) {
			// Specified by system property
			webAppDir = new File(webAppLocation);

		} else {
			// Not configured so derive from default location
			webAppDir = new File(projectDirectory, WEBAPP_PATH);
		}

		// Within maven project, so include webapp WoOF resources
		if (!(webAppDir.exists())) {
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
	 * Loads extension functionality from the {@link WoofExtensionService}
	 * instances.
	 * 
	 * @param webARchitect
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
	public static void loadWebApplicationExtensions(WebArchitect webARchitect, SourceProperties properties,
			ClassLoader classLoader, ResourceSource... resourceSources) throws Exception {

	}

	/**
	 * WoOF application {@link ResourceSource} instances.
	 */
	private final List<ResourceSource> applicationResourceSources = new LinkedList<ResourceSource>();

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

	/*
	 * ================= OfficeFloorExtensionService ===================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Load the optional configuration to the application
		String teamsLocation = context.getProperty(PROPERTY_TEAMS_CONFIGURATION_LOCATION,
				DEFAULT_TEAMS_CONFIGURATION_LOCATION);

		// Load the optional teams configuration to the application
		ConfigurationItem teamsConfiguration = context.getOptionalConfigurationItem(teamsLocation, null);
		if (teamsConfiguration != null) {
			// Load the teams configuration
			WoofTeamsLoader teamsLoader = new WoofTeamsLoaderImpl(
					new WoofTeamsRepositoryImpl(new ModelRepositoryImpl()));
			teamsLoader.loadWoofTeamsConfiguration(new WoofTeamsLoaderContext() {

				@Override
				public OfficeFloorExtensionContext getOfficeFloorExtensionContext() {
					return context;
				}

				@Override
				public OfficeFloorDeployer getOfficeFloorDeployer() {
					return officeFloorDeployer;
				}

				@Override
				public ConfigurationItem getConfiguration() {
					return teamsConfiguration;
				}
			});
		}
	}

	/*
	 * =================== OfficeExtensionService ======================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Employ the Web Architect
		WebArchitect web = WebArchitectEmployer.employWebArchitect(officeArchitect, context);

		// Provide default WoOF template suffix
		web.setDefaultHttpTemplateUriSuffix(WOOF_TEMPLATE_URI_SUFFIX);

		// Obtain the woof configuration (ensuring exists)
		String woofLocation = context.getProperty(PROPERTY_WOOF_CONFIGURATION_LOCATION,
				DEFAULT_WOOF_CONFIGUARTION_LOCATION);
		ConfigurationItem woofConfiguration = context.getConfigurationItem(woofLocation, null);
		if (woofConfiguration == null) {
			officeArchitect.addIssue("Can not find WoOF configuration file '" + woofLocation + "'");
			return; // must have WoOF configuration
		}

		// Load the WoOF configuration to the application
		WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(new ModelRepositoryImpl()));
		woofLoader.loadWoofConfiguration(new WoofLoaderContext() {
			@Override
			public OfficeExtensionContext getOfficeExtensionContext() {
				return context;
			}

			@Override
			public OfficeArchitect getOfficeArchitect() {
				return officeArchitect;
			}

			@Override
			public ConfigurationItem getConfiguration() {
				return woofConfiguration;
			}

			@Override
			public WebArchitect getWebArchitect() {
				return web;
			}
		});

		// Load the optional objects configuration to the application
		String objectsLocation = context.getProperty(PROPERTY_OBJECTS_CONFIGURATION_LOCATION,
				DEFAULT_OBJECTS_CONFIGURATION_LOCATION);
		final ConfigurationItem objectsConfiguration = context.getConfigurationItem(objectsLocation, null);
		if (objectsConfiguration != null) {

			// Load the objects configuration
			WoofObjectsLoader objectsLoader = new WoofObjectsLoaderImpl(
					new WoofObjectsRepositoryImpl(new ModelRepositoryImpl()));
			objectsLoader.loadWoofObjectsConfiguration(new WoofObjectsLoaderContext() {

				@Override
				public OfficeExtensionContext getOfficeExtensionContext() {
					return context;
				}

				@Override
				public OfficeArchitect getOfficeArchitect() {
					return officeArchitect;
				}

				@Override
				public ConfigurationItem getConfiguration() {
					return objectsConfiguration;
				}
			});
		}

		// Load the woof extensions
		ClassLoader classLoader = context.getClassLoader();
		ServiceLoader<WoofExtensionService> extensionServiceLoader = ServiceLoader.load(WoofExtensionService.class,
				classLoader);
		Iterator<WoofExtensionService> extensionIterator = extensionServiceLoader.iterator();
		while (extensionIterator.hasNext()) {

			// Obtain the next extension service
			WoofExtensionService extensionService;
			try {
				extensionService = extensionIterator.next();
			} catch (ServiceConfigurationError ex) {
				// Issue loading service
				officeArchitect.addIssue(ex.getMessage(), ex);

				// Not loaded, so continue onto next
				continue;
			}

			// Extend the application
			try {
				extensionService.extend(new WoofExtensionServiceContext() {

					@Override
					public WebArchitect getWebArchitect() {
						return web;
					}

					@Override
					public OfficeExtensionContext getOfficeExtensionContext() {
						return context;
					}

					@Override
					public OfficeArchitect getOfficeArchitect() {
						return officeArchitect;
					}
				});

			} catch (Throwable ex) {
				// Issue with service
				officeArchitect.addIssue(WoofExtensionService.class.getSimpleName() + " "
						+ extensionService.getClass().getName() + " configuration failure: " + ex.getMessage(), ex);
			}
		}
	}

}