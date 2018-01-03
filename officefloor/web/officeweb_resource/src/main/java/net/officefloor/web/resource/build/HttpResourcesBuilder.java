/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.resource.build;

import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.spi.ResourceTransformer;
import net.officefloor.web.resource.spi.ResourceTransformerService;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Builds the external {@link HttpResource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourcesBuilder {

	/**
	 * <p>
	 * Specifies the context path within the application to serve the
	 * {@link HttpResource} instances.
	 * <p>
	 * Should a context path not be specified, the {@link HttpResource}
	 * instances will be served from the root of the application.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	void setContextPath(String contextPath);

	/**
	 * Registers a {@link ResourceTransformer} to transform the resources.
	 * 
	 * @param transformer
	 *            {@link ResourceTransformer}.
	 */
	void addResourceTransformer(ResourceTransformer transformer);

	/**
	 * Registers a {@link ResourceTransformer} from a
	 * {@link ResourceTransformerService} to transform the resources.
	 * 
	 * @param name
	 *            Name of the {@link ResourceTransformerService} to create the
	 *            {@link ResourceTransformer}.
	 * 
	 * @see ResourceTransformerService
	 */
	void addResourceTransformer(String name);

	/**
	 * Specifies the default file names within a directory.
	 * 
	 * @param defaultResourceNames
	 *            Default file names within directory.
	 */
	void setDirectoryDefaultResourceNames(String... defaultResourceNames);

	/**
	 * <p>
	 * Specifies a particular {@link HttpSecurity} to secure the
	 * {@link HttpResource} instances.
	 * <p>
	 * Should this not be provided, then the default {@link HttpSecurity} is
	 * used if role access specified.
	 * 
	 * @param httpSecurityName
	 *            Name of the {@link HttpSecurity}.
	 */
	void setHttpSecurityName(String httpSecurityName);

	/**
	 * Adds a role to allow access.
	 * 
	 * @param role
	 *            Role to allow access.
	 */
	void addRole(String role);

}