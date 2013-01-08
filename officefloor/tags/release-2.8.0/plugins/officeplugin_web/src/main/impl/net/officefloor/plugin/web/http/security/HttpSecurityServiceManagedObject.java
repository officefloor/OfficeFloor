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
package net.officefloor.plugin.web.http.security;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link ManagedObject} for the {@link HttpSecurityService}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityServiceManagedObject<D extends Enum<D>> implements
		CoordinatingManagedObject<Indexed> {

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<D> source;

	/**
	 * Mapping of dependency index to dependency key.
	 */
	private final D[] dependencyKeyMapping;

	/**
	 * {@link HttpSecurityService}.
	 */
	private HttpSecurityService service;

	/**
	 * Initiate.
	 * 
	 * @param source
	 *            {@link HttpSecuritySource}.
	 * @param dependencyKeyMapping
	 *            Mapping of dependency index to dependency key.
	 */
	public HttpSecurityServiceManagedObject(HttpSecuritySource<D> source,
			D[] dependencyKeyMapping) {
		this.source = source;
		this.dependencyKeyMapping = dependencyKeyMapping;
	}

	/*
	 * ==================== CoordinatingManagedObject ======================
	 */

	@Override
	public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {

		// Obtain the always required dependencies
		ServerHttpConnection connection = (ServerHttpConnection) registry
				.getObject(0);
		HttpSession session = (HttpSession) registry.getObject(1);
		final int ADDITIONAL_DEPENDENCY_OFFSET = 2;

		// Obtain the additional dependencies
		Map<D, Object> dependencies;
		if (this.dependencyKeyMapping.length == 0) {
			// No dependencies
			dependencies = Collections.emptyMap();
		} else {
			// Create map for additional dependencies
			dependencies = new EnumMap<D, Object>(this.dependencyKeyMapping[0]
					.getDeclaringClass());

			// Load the additional dependencies
			for (int i = 0; i < this.dependencyKeyMapping.length; i++) {
				D key = this.dependencyKeyMapping[i];

				// Obtain the additional dependency
				Object dependency = registry
						.getObject(ADDITIONAL_DEPENDENCY_OFFSET + i);
				
				// Load the additional dependency
				dependencies.put(key, dependency);
			}
		}

		// Create the service
		this.service = new HttpSecurityServiceImpl<D>(this.source, connection,
				session, dependencies);
	}

	@Override
	public Object getObject() throws Throwable {
		return this.service;
	}

}