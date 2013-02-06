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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.security.type.HttpSecurityDependencyType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityFlowType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link WorkSource} for {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityWorkSource extends
		AbstractWorkSource<HttpSecurityWork> {

	/**
	 * Obtains the credentials class for the {@link HttpSecurityType}.
	 * 
	 * @param type
	 *            {@link HttpSecurityType}.
	 * @return Credentials class.
	 */
	public static Class<?> getCredentialsClass(HttpSecurityType<?, ?, ?, ?> type) {
		Class<?> credentialsType = type.getCredentialsClass();
		if (credentialsType == null) {
			credentialsType = Void.class;
		}
		return credentialsType;
	}

	/**
	 * Name of {@link Property} providing the key to the
	 * {@link HttpSecuritySource} from the {@link HttpSecurityConfigurator}.
	 */
	public static final String PROPERTY_HTTP_SECURITY_SOURCE_KEY = HttpAuthenticationManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY;

	/**
	 * Name of the {@link HttpChallengeTask}.
	 */
	public static final String TASK_CHALLENGE = "CHALLENGE";

	/**
	 * Name of the {@link ManagedObjectHttpAuthenticateTask}.
	 */
	public static final String TASK_MANAGED_OBJECT_AUTHENTICATE = "MANAGED_OBJECT_AUTHENTICATE";

	/**
	 * Name of the {@link StartApplicationHttpAuthenticateTask}.
	 */
	public static final String TASK_START_APPLICATION_AUTHENTICATE = "START_APPLICATION_AUTHENTICATE";

	/**
	 * Name of the {@link CompleteApplicationHttpAuthenticateTask}.
	 */
	public static final String TASK_COMPLETE_APPLICATION_AUTHENTICATE = "COMPLETE_APPLICATION_AUTHENTICATE";

	/*
	 * =================== WorkSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_HTTP_SECURITY_SOURCE_KEY,
				"HTTP Security Source Key");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpSecurityWork> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Retrieve the HTTP Security configuration
		String key = context.getProperty(PROPERTY_HTTP_SECURITY_SOURCE_KEY);
		HttpSecurityConfiguration<?, ?, ?, ?> configuration = HttpSecurityConfigurator
				.getHttpSecuritySource(key);

		// Provide the work factory
		HttpSecuritySource<?, ?, ?, ?> httpSecuritySource = configuration
				.getHttpSecuritySource();
		workTypeBuilder
				.setWorkFactory(new HttpSecurityWork(httpSecuritySource));

		// Obtain the HTTP Security Type
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = configuration
				.getHttpSecurityType();

		// Obtain the credentials type
		Class<?> credentialsType = getCredentialsClass(httpSecurityType);

		// Obtain the index order list of dependency types
		List<HttpSecurityDependencyType<?>> dependencyTypes = new ArrayList<HttpSecurityDependencyType<?>>(
				Arrays.asList(httpSecurityType.getDependencyTypes()));
		Collections.sort(dependencyTypes,
				new Comparator<HttpSecurityDependencyType<?>>() {
					@Override
					public int compare(HttpSecurityDependencyType<?> a,
							HttpSecurityDependencyType<?> b) {
						return a.getIndex() - b.getIndex();
					}
				});

		// Obtain the index order list of flow types
		List<HttpSecurityFlowType<?>> flowTypes = new ArrayList<HttpSecurityFlowType<?>>(
				Arrays.asList(httpSecurityType.getFlowTypes()));
		Collections.sort(flowTypes, new Comparator<HttpSecurityFlowType<?>>() {
			@Override
			public int compare(HttpSecurityFlowType<?> a,
					HttpSecurityFlowType<?> b) {
				return a.getIndex() - b.getIndex();
			}
		});

		// Add the managed object authentication task
		TaskTypeBuilder<Indexed, None> moAuthenticate = workTypeBuilder
				.addTaskType(TASK_MANAGED_OBJECT_AUTHENTICATE,
						new ManagedObjectHttpAuthenticateTask(), Indexed.class,
						None.class);
		moAuthenticate.addObject(TaskAuthenticateContext.class).setLabel(
				"TASK_AUTHENTICATE_CONTEXT");
		for (HttpSecurityDependencyType<?> dependencyType : dependencyTypes) {
			moAuthenticate.addObject(dependencyType.getDependencyType())
					.setLabel(
							"DEPENDENCY_" + dependencyType.getDependencyName());
		}

		// Add the challenge task
		TaskTypeBuilder<Indexed, Indexed> challenge = workTypeBuilder
				.addTaskType(TASK_CHALLENGE, new HttpChallengeTask(),
						Indexed.class, Indexed.class);
		challenge.addObject(HttpAuthenticationRequiredException.class)
				.setLabel("HTTP_AUTHENTICATION_REQUIRED_EXCEPTION");
		challenge.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		challenge.addObject(HttpSession.class).setLabel("HTTP_SESSION");
		challenge.addObject(HttpRequestState.class).setLabel(
				"HTTP_REQUEST_STATE");
		for (HttpSecurityDependencyType<?> dependencyType : dependencyTypes) {
			challenge.addObject(dependencyType.getDependencyType()).setLabel(
					"DEPENDENCY_" + dependencyType.getDependencyName());
		}
		TaskFlowTypeBuilder<Indexed> challengeFailureFlow = challenge.addFlow();
		challengeFailureFlow.setLabel("FAILURE");
		challengeFailureFlow.setArgumentType(Throwable.class);
		for (HttpSecurityFlowType<?> flowType : flowTypes) {
			TaskFlowTypeBuilder<?> flow = challenge.addFlow();
			flow.setArgumentType(flowType.getArgumentType());
			flow.setLabel("FLOW_" + flowType.getFlowName());
		}

		// Add the start application authentication task
		TaskTypeBuilder<StartApplicationHttpAuthenticateTask.Dependencies, StartApplicationHttpAuthenticateTask.Flows> appStart = workTypeBuilder
				.addTaskType(
						TASK_START_APPLICATION_AUTHENTICATE,
						new StartApplicationHttpAuthenticateTask(),
						StartApplicationHttpAuthenticateTask.Dependencies.class,
						StartApplicationHttpAuthenticateTask.Flows.class);
		appStart.addObject(credentialsType).setKey(
				StartApplicationHttpAuthenticateTask.Dependencies.CREDENTIALS);
		appStart.addObject(HttpAuthentication.class)
				.setKey(StartApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION);
		TaskFlowTypeBuilder<StartApplicationHttpAuthenticateTask.Flows> appStartFailureFlow = appStart
				.addFlow();
		appStartFailureFlow
				.setKey(StartApplicationHttpAuthenticateTask.Flows.FAILURE);
		appStartFailureFlow.setArgumentType(Throwable.class);

		// Add the complete application authentication task
		TaskTypeBuilder<CompleteApplicationHttpAuthenticateTask.Dependencies, CompleteApplicationHttpAuthenticateTask.Flows> appComplete = workTypeBuilder
				.addTaskType(
						TASK_COMPLETE_APPLICATION_AUTHENTICATE,
						new CompleteApplicationHttpAuthenticateTask(),
						CompleteApplicationHttpAuthenticateTask.Dependencies.class,
						CompleteApplicationHttpAuthenticateTask.Flows.class);
		appComplete
				.addObject(HttpAuthentication.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION);
		appComplete
				.addObject(ServerHttpConnection.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.SERVER_HTTP_CONNECTION);
		appComplete
				.addObject(HttpSession.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_SESSION);
		appComplete
				.addObject(HttpRequestState.class)
				.setKey(CompleteApplicationHttpAuthenticateTask.Dependencies.REQUEST_STATE);
		TaskFlowTypeBuilder<CompleteApplicationHttpAuthenticateTask.Flows> appCompleteFailureFlow = appComplete
				.addFlow();
		appCompleteFailureFlow
				.setKey(CompleteApplicationHttpAuthenticateTask.Flows.FAILURE);
		appCompleteFailureFlow.setArgumentType(Throwable.class);

	}

}