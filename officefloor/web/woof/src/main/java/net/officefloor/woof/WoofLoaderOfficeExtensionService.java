/*-
 * #%L
 * Web on OfficeFloor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.woof;

import java.util.Iterator;
import java.util.ServiceConfigurationError;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.json.JacksonHttpObjectParserServiceFactory;
import net.officefloor.web.json.JacksonHttpObjectResponderServiceFactory;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.template.build.WebTemplateArchitect;
import net.officefloor.web.template.build.WebTemplateArchitectEmployer;
import net.officefloor.woof.WoofLoaderSettings.WoofLoaderConfiguration;
import net.officefloor.woof.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.woof.model.resources.WoofResourcesRepositoryImpl;
import net.officefloor.woof.model.teams.WoofTeamsRepositoryImpl;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;
import net.officefloor.woof.objects.WoofObjectsLoader;
import net.officefloor.woof.objects.WoofObjectsLoaderContext;
import net.officefloor.woof.objects.WoofObjectsLoaderImpl;
import net.officefloor.woof.resources.WoofResourcesLoader;
import net.officefloor.woof.resources.WoofResourcesLoaderContext;
import net.officefloor.woof.resources.WoofResourcesLoaderImpl;
import net.officefloor.woof.teams.WoofTeamsLoader;
import net.officefloor.woof.teams.WoofTeamsLoaderImpl;
import net.officefloor.woof.teams.WoofTeamsUsageContext;

/**
 * {@link OfficeExtensionService} for the {@link WoofLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/*
	 * =============== OfficeExtensionServiceFactory ===================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== OfficeExtensionService ======================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Obtain the WoOF loader configuration
		String officeName = context.getOfficeName();
		WoofLoaderConfiguration configuration = WoofLoaderSettings.getWoofLoaderConfiguration(officeName);

		// Determine if WoOF application
		if (!configuration.isWoofApplication(context)) {
			return; // not WoOF application
		}

		// Employ the architects
		WebArchitect web = WebArchitectEmployer.employWebArchitect(officeArchitect, context);
		HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, officeArchitect,
				context);
		WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(web, officeArchitect, context);
		HttpResourceArchitect resources = HttpResourceArchitectEmployer.employHttpResourceArchitect(web, security,
				officeArchitect, context);
		ProcedureArchitect<OfficeSection> procedure = ProcedureEmployer.employProcedureArchitect(officeArchitect,
				context);

		// Load the default object parser / responders
		web.setDefaultHttpObjectParser(new JacksonHttpObjectParserServiceFactory());
		web.setDefaultHttpObjectResponder(new JacksonHttpObjectResponderServiceFactory());

		// Create the WoOF context
		WoofContext woofContext = new WoofContext() {

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
				return configuration.getWoofConfiguration(context);
			}

			@Override
			public WebArchitect getWebArchitect() {
				return web;
			}

			@Override
			public HttpSecurityArchitect getHttpSecurityArchitect() {
				return security;
			}

			@Override
			public WebTemplateArchitect getWebTemplater() {
				return templater;
			}

			@Override
			public HttpResourceArchitect getHttpResourceArchitect() {
				return resources;
			}

			@Override
			public ProcedureArchitect<OfficeSection> getProcedureArchitect() {
				return procedure;
			}
		};

		// Load the WoOF configuration to the application
		if (configuration.isLoadWoof() && configuration.isApplicationWoofAvailable(context)) {
			WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(new ModelRepositoryImpl()));
			woofLoader.loadWoofConfiguration(woofContext);
		}

		// Load the optional objects configuration to the application
		if (configuration.isLoadObjects()) {
			final ConfigurationItem objectsConfiguration = configuration.getObjectsConfiguration(context);
			if (objectsConfiguration != null) {

				// Indicate loading teams
				if (!configuration.isContextualLoad()) {
					context.getLogger().info("Loading WoOF objects");
				}

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
		}

		// Load the optional resources configuration to the application
		if (configuration.isLoadResources()) {
			final ConfigurationItem resourcesConfiguration = configuration.getResourcesConfiguration(context);
			if (resourcesConfiguration != null) {

				// Indicate loading teams
				if (!configuration.isContextualLoad()) {
					context.getLogger().info("Loading WoOF resources");
				}

				// Load the resources configuration
				WoofResourcesLoader resourcesLoader = new WoofResourcesLoaderImpl(
						new WoofResourcesRepositoryImpl(new ModelRepositoryImpl()));
				resourcesLoader.loadWoofResourcesConfiguration(new WoofResourcesLoaderContext() {

					@Override
					public OfficeExtensionContext getOfficeExtensionContext() {
						return context;
					}

					@Override
					public OfficeArchitect getOfficeArchitect() {
						return officeArchitect;
					}

					@Override
					public HttpResourceArchitect getHttpResourceArchitect() {
						return resources;
					}

					@Override
					public ConfigurationItem getConfiguration() {
						return resourcesConfiguration;
					}
				});
			}
		}

		// Load the optional teams configuration for the application
		if (configuration.isLoadTeams()) {
			ConfigurationItem teamsConfiguration = configuration.getTeamsConfiguration(context);
			if (teamsConfiguration != null) {

				// Load the teams configuration
				WoofTeamsLoader teamsLoader = new WoofTeamsLoaderImpl(
						new WoofTeamsRepositoryImpl(new ModelRepositoryImpl()));
				teamsLoader.loadWoofTeamsUsage(new WoofTeamsUsageContext() {

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
						return teamsConfiguration;
					}
				});
			}
		}

		// Load the woof extensions
		if (configuration.isLoadWoofExtensions()) {
			Iterator<WoofExtensionService> extensionIterator = context
					.loadOptionalServices(WoofExtensionServiceFactory.class).iterator();
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
					extensionService.extend(woofContext);

				} catch (Throwable ex) {
					// Issue with service
					officeArchitect.addIssue(WoofLoaderSettings.class.getSimpleName() + " "
							+ extensionService.getClass().getName() + " configuration failure: " + ex.getMessage(), ex);
				}
			}
		}

		// Load the contextual extensions
		for (WoofExtensionService contextualExtension : configuration.getContextualWoofExtensionServices()) {
			contextualExtension.extend(woofContext);
		}

		// Inform Office Architect
		templater.informWebArchitect();
		resources.informWebArchitect();
		security.informWebArchitect();
		web.informOfficeArchitect();
	}

}
