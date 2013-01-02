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

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;

/**
 * <p>
 * {@link ManagedObjectSource} for the {@link HttpSecurity}.
 * <p>
 * Should the {@link HttpRequest} not be authenticated then <code>null</code>
 * will be provided. Otherwise the {@link HttpSecurity} is provided.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObjectSource
		extends
		AbstractManagedObjectSource<HttpSecurityManagedObjectSource.DependencyKeys, HttpSecurityManagedObjectSource.FlowKeys>
		implements
		WorkFactory<HttpSecurityManagedObjectSource>,
		Work,
		TaskFactory<HttpSecurityManagedObjectSource, HttpSecurityManagedObjectSource.AuthenticateTaskDependencyKeys, None>,
		Task<HttpSecurityManagedObjectSource, HttpSecurityManagedObjectSource.AuthenticateTaskDependencyKeys, None> {

	/**
	 * Name of the {@link Team} for authentication.
	 */
	public static final String TEAM_AUTHENTICATOR = "AUTHENTICATOR";

	/**
	 * Dependency keys for the {@link HttpSecurityManagedObjectSource}.
	 */
	public static enum DependencyKeys {
		HTTP_SECURITY_SERVICE
	}

	/**
	 * Flow keys for the {@link HttpSecurityManagedObjectSource}.
	 */
	public static enum FlowKeys {
		AUTHENTICATE
	}

	/**
	 * Dependency keys for the {@link Task} to authenticate.
	 */
	public static enum AuthenticateTaskDependencyKeys {
		HTTP_SECURITY_MANAGED_OBJECT
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<FlowKeys> executeContext;

	/**
	 * Triggers the authentication for the {@link HttpSecurityManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link HttpSecurityManagedObject} to undertake authentication.
	 */
	public void triggerAuthentication(HttpSecurityManagedObject managedObject) {
		// Must provide new managed object to not override asynchronous listener
		this.executeContext.invokeProcess(FlowKeys.AUTHENTICATE, managedObject,
				new HttpSecurityManagedObject(this), 0);
	}

	/*
	 * ===================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<DependencyKeys, FlowKeys> context) throws Exception {
		ManagedObjectSourceContext<FlowKeys> mosContext = context
				.getManagedObjectSourceContext();

		// Require the HTTP Security Service
		context.addDependency(DependencyKeys.HTTP_SECURITY_SERVICE,
				HttpSecurityService.class);

		// Add flow for authenticating
		context.addFlow(FlowKeys.AUTHENTICATE, HttpSecurityManagedObject.class);

		// Register the task to authenticate
		final String WORK_NAME = "Authentication";
		final String TASK_NAME = "Authenticate";
		ManagedObjectWorkBuilder<HttpSecurityManagedObjectSource> work = mosContext
				.addWork(WORK_NAME, this);
		ManagedObjectTaskBuilder<AuthenticateTaskDependencyKeys, None> task = work
				.addTask(TASK_NAME, this);
		task.linkParameter(
				AuthenticateTaskDependencyKeys.HTTP_SECURITY_MANAGED_OBJECT,
				HttpSecurityManagedObject.class);
		task.setTeam(TEAM_AUTHENTICATOR);

		// Allow triggering the authentication task
		mosContext.linkProcess(FlowKeys.AUTHENTICATE, WORK_NAME, TASK_NAME);

		// Specify the meta-data
		context.setObjectClass(HttpSecurity.class);
		context.setManagedObjectClass(HttpSecurityManagedObject.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<FlowKeys> context)
			throws Exception {
		this.executeContext = context;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpSecurityManagedObject(this);
	}

	/*
	 * ======================= WorkFactory ==============================
	 */

	@Override
	public HttpSecurityManagedObjectSource createWork() {
		return this;
	}

	/*
	 * ======================== TaskFactory =============================
	 */

	@Override
	public Task<HttpSecurityManagedObjectSource, AuthenticateTaskDependencyKeys, None> createTask(
			HttpSecurityManagedObjectSource work) {
		return work;
	}

	/*
	 * =========================== Task ==================================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpSecurityManagedObjectSource, AuthenticateTaskDependencyKeys, None> context)
			throws IOException, AuthenticationException {

		// Obtain the HTTP Security Managed Object
		HttpSecurityManagedObject managedObject = (HttpSecurityManagedObject) context
				.getObject(AuthenticateTaskDependencyKeys.HTTP_SECURITY_MANAGED_OBJECT);

		// Undertake the authentication
		managedObject.authenticate();

		// Nothing to return
		return null;
	}

}