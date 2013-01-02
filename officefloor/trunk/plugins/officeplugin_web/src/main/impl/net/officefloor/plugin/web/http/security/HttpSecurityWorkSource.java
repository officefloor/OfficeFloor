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

import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.HttpSecurityTask.DependencyKeys;
import net.officefloor.plugin.web.http.security.HttpSecurityTask.FlowKeys;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;

/**
 * <p>
 * {@link WorkSource} to handle {@link HttpSecurity} and direct flow based on
 * authentication.
 * <p>
 * If unauthenticated then will also load the unauthorised details to the
 * {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityWorkSource extends
		AbstractWorkSource<HttpSecurityTask> {

	/*
	 * ====================== WorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpSecurityTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Create the authentication Task factory
		HttpSecurityTask factory = new HttpSecurityTask();

		// Register the work
		workTypeBuilder.setWorkFactory(factory);

		// Add the authentication task
		TaskTypeBuilder<DependencyKeys, FlowKeys> task = workTypeBuilder
				.addTaskType("Authenticate", factory, DependencyKeys.class,
						FlowKeys.class);
		task.addObject(HttpSecurityService.class).setKey(
				DependencyKeys.HTTP_SECURITY_SERVICE);
		TaskFlowTypeBuilder<FlowKeys> authenticateFlow = task.addFlow();
		authenticateFlow.setKey(FlowKeys.AUTHENTICATED);
		authenticateFlow.setArgumentType(HttpSecurity.class);
		task.addFlow().setKey(FlowKeys.UNAUTHENTICATED);
		task.setReturnType(HttpSecurity.class);
		task.addEscalation(AuthenticationException.class);
	}

}