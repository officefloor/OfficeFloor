/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.resources;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.resource.build.HttpResourcesBuilder;
import net.officefloor.web.security.build.HttpSecurableBuilder;
import net.officefloor.woof.model.resources.TypeQualificationModel;
import net.officefloor.woof.model.resources.WoofResourceModel;
import net.officefloor.woof.model.resources.WoofResourceSecurityModel;
import net.officefloor.woof.model.resources.WoofResourceTransformerModel;
import net.officefloor.woof.model.resources.WoofResourcesModel;
import net.officefloor.woof.model.resources.WoofResourcesRepository;

/**
 * {@link WoofResourcesLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofResourcesLoaderImpl implements WoofResourcesLoader {

	/**
	 * {@link WoofResourcesRepository}.
	 */
	private final WoofResourcesRepository repository;

	/**
	 * Instantiate.
	 * 
	 * @param repository
	 *            {@link WoofResourcesRepository}.
	 */
	public WoofResourcesLoaderImpl(WoofResourcesRepository repository) {
		this.repository = repository;
	}

	/*
	 * ==================== WoofResourcesLoader ======================
	 */

	@Override
	public void loadWoofResourcesConfiguration(WoofResourcesLoaderContext context) throws Exception {

		// Obtain the details
		ConfigurationItem resourcesConfiguration = context.getConfiguration();

		// Obtain the Office Architect and context
		HttpResourceArchitect architect = context.getHttpResourceArchitect();

		// Load the resources model
		WoofResourcesModel resources = new WoofResourcesModel();
		this.repository.retrieveWoofResources(resources, resourcesConfiguration);

		// Configure the resources
		for (WoofResourceModel resource : resources.getWoofResources()) {

			// Generate the protocol / location string
			String protocol = resource.getProtocol();
			String location = resource.getLocation();
			String protocolLocation = (CompileUtil.isBlank(protocol) ? "" : protocol + ":") + location;

			// Add the resource
			HttpResourcesBuilder builder = architect.addHttpResources(protocolLocation);

			// Configure context path
			String contextPath = resource.getContextPath();
			if (!CompileUtil.isBlank(contextPath)) {
				builder.setContextPath(contextPath);
			}

			// Load the qualifiers
			for (TypeQualificationModel qualifier : resource.getTypeQualifications()) {
				builder.addTypeQualifier(qualifier.getQualifier());
			}

			// Load the resource transformers
			for (WoofResourceTransformerModel transformer : resource.getWoofResourceTransformers()) {
				builder.addResourceTransformer(transformer.getName());
			}

			// Load possible security
			WoofResourceSecurityModel security = resource.getSecurity();
			if (security != null) {

				// Obtain the securer builder
				HttpSecurableBuilder securer = builder.getHttpSecurer();

				// Provide possibly security qualifier
				String httpSecurityName = security.getHttpSecurityName();
				if (!CompileUtil.isBlank(httpSecurityName)) {
					securer.setHttpSecurityName(httpSecurityName);
				}

				// Load the roles
				for (String role : security.getRoles()) {
					securer.addRole(role);
				}

				// Load the required roles
				for (String requiredRole : security.getRequiredRoles()) {
					securer.addRequiredRole(requiredRole);
				}
			}
		}
	}

}
