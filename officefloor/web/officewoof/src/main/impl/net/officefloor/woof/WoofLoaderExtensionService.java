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
package net.officefloor.woof;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerConfigurationService;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.woof.WoofExtensionService;
import net.officefloor.woof.WoofExtensionServiceContext;
import net.officefloor.woof.WoofLoader;
import net.officefloor.woof.WoofLoaderContext;
import net.officefloor.woof.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.woof.model.teams.WoofTeamsRepositoryImpl;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;
import net.officefloor.woof.objects.WoofObjectsLoaderImpl;
import net.officefloor.woof.plugin.objects.WoofObjectsLoader;
import net.officefloor.woof.plugin.objects.WoofObjectsLoaderContext;
import net.officefloor.woof.teams.WoofTeamsLoader;
import net.officefloor.woof.teams.WoofTeamsLoaderContext;
import net.officefloor.woof.teams.WoofTeamsLoaderImpl;

/**
 * {@link OfficeFloorExtensionService} / {@link OfficeExtensionService} to
 * configure the {@link WoofModel} into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderExtensionService
		implements OfficeFloorCompilerConfigurationService, OfficeFloorExtensionService, OfficeExtensionService {

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
	 * Creates the {@link ResourceSource} instances for the
	 * <code>src/main/web/<code> resources.
	 * 
	 * @param projectDirectory
	 *            Directory of the project.
	 * @return {@link ResourceSource} instances.
	 */
	public static ResourceSource[] createResourceSources(File projectDirectory) {

		// Determine if running within maven project
		if (!(new File(projectDirectory, "pom.xml").exists())) {
			return new ResourceSource[0]; // must be a maven project
		}

		// Obtain the web app directory
		File webAppDir = new File(projectDirectory, WEBAPP_PATH);
		if (!(webAppDir.exists())) {
			return new ResourceSource[0]; // not include
		}

		// Create listing of sources
		ResourceSource[] sources = new ResourceSource[2];

		// Make WoOF resources available
		sources[0] = new ResourceSource() {
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
					// Failed to obtain content so no resource
					return null;
				}

				// Not found within webapp directory
				return null;
			}
		};

		// Include all webapp directory resources for application extension
		sources[1] = new ResourceSource() {
			@Override
			public InputStream sourceResource(String location) {

				// Determine if resource exists
				File resource = new File(webAppDir, location);
				if (!(resource.exists())) {
					return null; // resource not exist
				}

				// Return content of resource
				try {
					return new FileInputStream(resource);

				} catch (IOException ex) {
					// Failed to obtain content so no resource
					return null;
				}
			}
		};

		// Return the sources
		return sources;
	}

	/**
	 * Configures the {@link OfficeFloorCompiler} for <code>src/main/web/</code>
	 * resources.
	 * 
	 * @param projectDirectory
	 *            Directory of the project.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public static void configureOfficeFloorCompiler(File projectDirectory, OfficeFloorCompiler compiler) {

		// Load the resource sources
		for (ResourceSource source : createResourceSources(projectDirectory)) {
			compiler.addResources(source);
		}
	}

	/*
	 * =========== OfficeFloorCompilerConfigurationService =============
	 */

	@Override
	public void configureOfficeFloorCompiler(OfficeFloorCompiler compiler) throws Exception {

		// Obtain the current directory
		File currentDirectory = new File(".");

		// Configure the compiler
		configureOfficeFloorCompiler(currentDirectory, compiler);
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
				officeArchitect.addIssue(WoofLoaderExtensionService.class.getSimpleName() + " "
						+ extensionService.getClass().getName() + " configuration failure: " + ex.getMessage(), ex);
			}
		}
	}

}