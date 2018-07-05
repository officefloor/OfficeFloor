/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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