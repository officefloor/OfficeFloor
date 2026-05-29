/*-
 * #%L
 * Web on OfficeFloor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.function.Function;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.activity.govern.build.GovernanceArchitect;
import net.officefloor.activity.govern.build.GovernanceEmployer;
import net.officefloor.activity.managedobject.build.ManagedObjectArchitect;
import net.officefloor.activity.managedobject.build.ManagedObjectEmployer;
import net.officefloor.activity.supplier.build.SupplierArchitect;
import net.officefloor.activity.supplier.build.SupplierEmployer;
import net.officefloor.activity.team.build.TeamEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeGovernance;
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
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.rest.HttpSecurityRestMethodDecorator;
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

	public static final String OFFICE_FLOOR_DIRECTORY_PROPERTY = "officefloor.directory";
	public static final String REST_DIRECTORY_PROPERTY = "officefloor.rest.directory";
	public static final String OBJECTS_DIRECTORY_PROPERTY = "officefloor.objects.directory";
	public static final String SUPPLIERS_DIRECTORY_PROPERTY = "officefloor.suppliers.directory";
	public static final String GOVERN_DIRECTORY_PROPERTY = "officefloor.govern.directory";
	public static final String SECURITY_DIRECTORY_PROPERTY = "officefloor.security.directory";
	public static final String TEAMS_DIRECTORY_PROPERTY = "officefloor.teams.directory";

	public static final String OFFICE_FLOOR_DIRECTORY_TAG = "${officefloor}";

	public static final String OFFICE_FLOOR_DEFAULT_DIRECTORY = "officefloor";
	public static final String REST_DEFAULT_DIRECTORY = OFFICE_FLOOR_DIRECTORY_TAG + "/rest";
	public static final String OBJECTS_DEFAULT_DIRECTORY = OFFICE_FLOOR_DIRECTORY_TAG + "/objects";
	public static final String SUPPLIERS_DEFAULT_DIRECTORY = OFFICE_FLOOR_DIRECTORY_TAG + "/suppliers";
	public static final String GOVERN_DEFAULT_DIRECTORY = OFFICE_FLOOR_DIRECTORY_TAG + "/govern";
	public static final String SECURITY_DEFAULT_DIRECTORY = OFFICE_FLOOR_DIRECTORY_TAG + "/security";
	public static final String TEAMS_DEFAULT_DIRECTORY = OFFICE_FLOOR_DIRECTORY_TAG + "/teams";

	public static String interpolateRestDirectory(String officeFloorDirectory, String interpolateDirectory) {
		return interpolateDirectory.replace(OFFICE_FLOOR_DIRECTORY_TAG, officeFloorDirectory);
	}

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

	@FunctionalInterface
	private static interface InformOfficeArchitect {
		void inform() throws Exception;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Obtain the WoOF loader configuration
		String officeName = context.getOfficeName();
		WoofLoaderConfiguration configuration = WoofLoaderSettings.getWoofLoaderConfiguration(officeName);

		// Determine if load
		if (!configuration.isLoad()) {
			return;
		}

		// Employ the architects
		WebArchitect web = WebArchitectEmployer.employWebArchitect(officeArchitect, context);
		ComposeArchitect compose = ComposeEmployer.employComposeArchitect(officeArchitect, context);
		HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, compose, officeArchitect,
				context);
		RestArchitect rest = RestEmployer.employRestArchitect(officeArchitect, web, compose, context);
		WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(web, officeArchitect, context);
		HttpResourceArchitect resources = HttpResourceArchitectEmployer.employHttpResourceArchitect(web, security,
				officeArchitect, context);
		ProcedureArchitect<OfficeSection> procedure = ProcedureEmployer.employProcedureArchitect(officeArchitect,
				context);

		// Obtain the compose directories (overridable via properties)
		String officeFloorDirectory = context.getProperty(OFFICE_FLOOR_DIRECTORY_PROPERTY, OFFICE_FLOOR_DEFAULT_DIRECTORY);
		String restDirectory = interpolateRestDirectory(officeFloorDirectory, context.getProperty(REST_DIRECTORY_PROPERTY, REST_DEFAULT_DIRECTORY));
		String objectsDirectory = interpolateRestDirectory(officeFloorDirectory, context.getProperty(OBJECTS_DIRECTORY_PROPERTY, OBJECTS_DEFAULT_DIRECTORY));
		String suppliersDirectory = interpolateRestDirectory(officeFloorDirectory, context.getProperty(SUPPLIERS_DIRECTORY_PROPERTY, SUPPLIERS_DEFAULT_DIRECTORY));
		String governDirectory = interpolateRestDirectory(officeFloorDirectory, context.getProperty(GOVERN_DIRECTORY_PROPERTY, GOVERN_DEFAULT_DIRECTORY));
		String securityDirectory = interpolateRestDirectory(officeFloorDirectory, context.getProperty(SECURITY_DIRECTORY_PROPERTY, SECURITY_DEFAULT_DIRECTORY));
		String teamsDirectory = interpolateRestDirectory(officeFloorDirectory, context.getProperty(TEAMS_DIRECTORY_PROPERTY, TEAMS_DEFAULT_DIRECTORY));

		// Pre-load external WoOF extension services (also used for WoOF application detection)
		List<WoofExtensionService> externalWoofExtensions = null;
		if (configuration.isLoadWoofExtensions()) {
			externalWoofExtensions = new ArrayList<>();
			Iterator<WoofExtensionService> checkIterator = context
					.loadOptionalServices(WoofExtensionServiceFactory.class).iterator();
			while (checkIterator.hasNext()) {
				try {
					externalWoofExtensions.add(checkIterator.next());
				} catch (ServiceConfigurationError ex) {
					officeArchitect.addIssue(ex.getMessage(), ex);
				}
			}
		}

		// Determine if WoOF application (including check for external WoOF extensions)
		if ((!configuration.isWoofApplication(context)) && (!rest.isRestAvailable(restDirectory))) {
			if (externalWoofExtensions == null || externalWoofExtensions.isEmpty()) {
				return; // not WoOF application
			}
		}

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

		// Build property list for compose configuration
		PropertyList composeProperties = context.createPropertyList();
		for (String propName : context.getPropertyNames()) {
			composeProperties.addProperty(propName).setValue(context.getProperty(propName));
		}

		// Load the WoOF configuration to the application
		if (configuration.isLoadWoof()) {

			// Load via model (if available)
			if (configuration.isApplicationWoofAvailable(context)) {
				WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(new ModelRepositoryImpl()));
				woofLoader.loadWoofConfiguration(woofContext);
			}

			// Load governance
			GovernanceArchitect governanceArchitect = GovernanceEmployer.employGovernanceArchitect(officeArchitect, compose, context);
			Map<String, OfficeGovernance> governances = governanceArchitect.addGovernances(governDirectory, composeProperties);
			governances.forEach(compose::addGovernance);

			// Load the HTTP Security and configure security
			Map<String, HttpSecurityBuilder> securityBuilders = security.addHttpSecurities(securityDirectory, composeProperties);
			rest.addRestMethodDecorator(new HttpSecurityRestMethodDecorator(securityBuilders));
		}

		// Load the optional objects configuration to the application
		if (configuration.isLoadObjects()) {

			// Load via model (if available)
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

			// Load the composition managed objects
			ManagedObjectArchitect managedObjectArchitect = ManagedObjectEmployer.employManagedObjectArchitect(officeArchitect, compose, context);
			managedObjectArchitect.addManagedObjects(objectsDirectory, composeProperties);

			// Load the composition suppliers
			SupplierArchitect supplierArchitect = SupplierEmployer.employSupplierArchitect(officeArchitect, context);
			supplierArchitect.addSuppliers(suppliersDirectory, composeProperties);
		}

		// Load the optional resources configuration to the application
		if (configuration.isLoadResources()) {

			// Load via model (if available)
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

			// Load via model (application.teams XML) if available
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

		// Load the woof extensions (use pre-loaded services to avoid double-loading)
		if (externalWoofExtensions != null) {
			for (WoofExtensionService extensionService : externalWoofExtensions) {

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

		// Load the REST services
		if (configuration.isLoadWoof()) {
			rest.addRestServices(false, restDirectory, composeProperties);
		}

		// Inform of web
		web.informOfficeArchitect();
	}

}
