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
package net.officefloor.plugin.web.http.secure;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.secure.HttpSecureTask.HttpSecureTaskDependencies;
import net.officefloor.plugin.web.http.secure.HttpSecureTask.HttpSecureTaskFlows;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link WorkSource} to provide appropriately secure
 * {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecureWorkSource extends AbstractWorkSource<HttpSecureTask> {

	/**
	 * Prefix name of the {@link Property} instances for the secure paths.
	 */
	public static final String PROPERTY_PREFIX_SECURE_PATH = "http.secure.path.";

	/*
	 * ==================== WorkSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpSecureTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the secure paths
		Set<String> securePaths = new HashSet<String>();
		for (String propertyName : context.getPropertyNames()) {
			if (propertyName.startsWith(PROPERTY_PREFIX_SECURE_PATH)) {
				String securePath = context.getProperty(propertyName);
				securePaths.add(securePath);
			}
		}

		// Configure the work factory
		HttpSecureTask factory = new HttpSecureTask(securePaths);
		workTypeBuilder.setWorkFactory(factory);

		// Configure the task
		TaskTypeBuilder<HttpSecureTaskDependencies, HttpSecureTaskFlows> task = workTypeBuilder
				.addTaskType("secure", factory,
						HttpSecureTaskDependencies.class,
						HttpSecureTaskFlows.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpSecureTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addObject(HttpApplicationLocation.class).setKey(
				HttpSecureTaskDependencies.HTTP_APPLICATION_LOCATION);
		task.addObject(HttpSession.class).setKey(
				HttpSecureTaskDependencies.HTTP_SESSION);
		task.addFlow().setKey(HttpSecureTaskFlows.SERVICE);
	}

}