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

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.security.type.HttpSecurityDependencyType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityFlowType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunctionSource} for {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedFunctionSource extends AbstractManagedFunctionSource {

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
	 * Name of the {@link HttpChallengeFunction}.
	 */
	public static final String FUNCTION_CHALLENGE = "CHALLENGE";

	/**
	 * Name of the {@link ManagedObjectHttpAuthenticateFunction}.
	 */
	public static final String FUNCTION_MANAGED_OBJECT_AUTHENTICATE = "MANAGED_OBJECT_AUTHENTICATE";

	/**
	 * Name of the {@link ManagedObjectHttpLogoutFunction}.
	 */
	public static final String FUNCTION_MANAGED_OBJECT_LOGOUT = "MANAGED_OBJECT_LOGOUT";

	/**
	 * Name of the {@link StartApplicationHttpAuthenticateFunction}.
	 */
	public static final String FUNCTION_START_APPLICATION_AUTHENTICATE = "START_APPLICATION_AUTHENTICATE";

	/**
	 * Name of the {@link CompleteApplicationHttpAuthenticateFunction}.
	 */
	public static final String FUNCTION_COMPLETE_APPLICATION_AUTHENTICATE = "COMPLETE_APPLICATION_AUTHENTICATE";

	/**
	 * {@link HttpSecurityConfiguration}.
	 */
	private final HttpSecurityConfiguration<?, ?, ?, ?> httpSecurityConfiguration;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityConfiguration
	 *            {@link HttpSecurityConfiguration}.
	 */
	public HttpSecurityManagedFunctionSource(HttpSecurityConfiguration<?, ?, ?, ?> httpSecurityConfiguration) {
		this.httpSecurityConfiguration = httpSecurityConfiguration;
	}

	/*
	 * =================== WorkSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the HTTP security source
		HttpSecuritySource<?, ?, ?, ?> httpSecuritySource = this.httpSecurityConfiguration.getHttpSecuritySource();

		// Obtain the HTTP Security Type
		HttpSecurityType<?, ?, ?, ?> httpSecurityType = this.httpSecurityConfiguration.getHttpSecurityType();

		// Obtain the credentials type
		Class<?> credentialsType = getCredentialsClass(httpSecurityType);

		// Obtain the index order list of dependency types
		List<HttpSecurityDependencyType<?>> dependencyTypes = new ArrayList<HttpSecurityDependencyType<?>>(
				Arrays.asList(httpSecurityType.getDependencyTypes()));
		Collections.sort(dependencyTypes, new Comparator<HttpSecurityDependencyType<?>>() {
			@Override
			public int compare(HttpSecurityDependencyType<?> a, HttpSecurityDependencyType<?> b) {
				return a.getIndex() - b.getIndex();
			}
		});

		// Obtain the index order list of flow types
		List<HttpSecurityFlowType<?>> flowTypes = new ArrayList<HttpSecurityFlowType<?>>(
				Arrays.asList(httpSecurityType.getFlowTypes()));
		Collections.sort(flowTypes, new Comparator<HttpSecurityFlowType<?>>() {
			@Override
			public int compare(HttpSecurityFlowType<?> a, HttpSecurityFlowType<?> b) {
				return a.getIndex() - b.getIndex();
			}
		});

		// Add the managed object authentication function
		ManagedFunctionTypeBuilder<Indexed, None> moAuthenticate = namespaceTypeBuilder.addManagedFunctionType(
				FUNCTION_MANAGED_OBJECT_AUTHENTICATE, new ManagedObjectHttpAuthenticateFunction(httpSecuritySource),
				Indexed.class, None.class);
		moAuthenticate.addObject(FunctionAuthenticateContext.class).setLabel("TASK_AUTHENTICATE_CONTEXT");
		for (HttpSecurityDependencyType<?> dependencyType : dependencyTypes) {
			moAuthenticate.addObject(dependencyType.getDependencyType())
					.setLabel("DEPENDENCY_" + dependencyType.getDependencyName());
		}

		// Add the managed object logout function
		ManagedFunctionTypeBuilder<Indexed, None> logout = namespaceTypeBuilder.addManagedFunctionType(
				FUNCTION_MANAGED_OBJECT_LOGOUT, new ManagedObjectHttpLogoutFunction(httpSecuritySource), Indexed.class,
				None.class);
		logout.addObject(FunctionLogoutContext.class).setLabel("TASK_LOGOUT_CONTEXT");
		for (HttpSecurityDependencyType<?> dependencyType : dependencyTypes) {
			logout.addObject(dependencyType.getDependencyType())
					.setLabel("DEPENDENCY_" + dependencyType.getDependencyName());
		}

		// Add the challenge function
		ManagedFunctionTypeBuilder<Indexed, Indexed> challenge = namespaceTypeBuilder.addManagedFunctionType(
				FUNCTION_CHALLENGE, new HttpChallengeFunction(httpSecuritySource), Indexed.class, Indexed.class);
		challenge.addObject(HttpAuthenticationRequiredException.class)
				.setLabel("HTTP_AUTHENTICATION_REQUIRED_EXCEPTION");
		challenge.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		challenge.addObject(HttpSession.class).setLabel("HTTP_SESSION");
		challenge.addObject(HttpRequestState.class).setLabel("HTTP_REQUEST_STATE");
		for (HttpSecurityDependencyType<?> dependencyType : dependencyTypes) {
			challenge.addObject(dependencyType.getDependencyType())
					.setLabel("DEPENDENCY_" + dependencyType.getDependencyName());
		}
		ManagedFunctionFlowTypeBuilder<Indexed> challengeFailureFlow = challenge.addFlow();
		challengeFailureFlow.setLabel("FAILURE");
		challengeFailureFlow.setArgumentType(Throwable.class);
		for (HttpSecurityFlowType<?> flowType : flowTypes) {
			ManagedFunctionFlowTypeBuilder<?> flow = challenge.addFlow();
			flow.setArgumentType(flowType.getArgumentType());
			flow.setLabel("FLOW_" + flowType.getFlowName());
		}

		// Add the start application authentication task
		ManagedFunctionTypeBuilder<StartApplicationHttpAuthenticateFunction.Dependencies, StartApplicationHttpAuthenticateFunction.Flows> appStart = namespaceTypeBuilder
				.addManagedFunctionType(FUNCTION_START_APPLICATION_AUTHENTICATE,
						new StartApplicationHttpAuthenticateFunction(),
						StartApplicationHttpAuthenticateFunction.Dependencies.class,
						StartApplicationHttpAuthenticateFunction.Flows.class);
		appStart.addObject(credentialsType).setKey(StartApplicationHttpAuthenticateFunction.Dependencies.CREDENTIALS);
		appStart.addObject(HttpAuthentication.class)
				.setKey(StartApplicationHttpAuthenticateFunction.Dependencies.HTTP_AUTHENTICATION);
		ManagedFunctionFlowTypeBuilder<StartApplicationHttpAuthenticateFunction.Flows> appStartFailureFlow = appStart
				.addFlow();
		appStartFailureFlow.setKey(StartApplicationHttpAuthenticateFunction.Flows.FAILURE);
		appStartFailureFlow.setArgumentType(Throwable.class);

		// Add the complete application authentication task
		ManagedFunctionTypeBuilder<CompleteApplicationHttpAuthenticateFunction.Dependencies, CompleteApplicationHttpAuthenticateFunction.Flows> appComplete = namespaceTypeBuilder
				.addManagedFunctionType(FUNCTION_COMPLETE_APPLICATION_AUTHENTICATE,
						new CompleteApplicationHttpAuthenticateFunction(),
						CompleteApplicationHttpAuthenticateFunction.Dependencies.class,
						CompleteApplicationHttpAuthenticateFunction.Flows.class);
		appComplete.addObject(HttpAuthentication.class)
				.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_AUTHENTICATION);
		appComplete.addObject(ServerHttpConnection.class)
				.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.SERVER_HTTP_CONNECTION);
		appComplete.addObject(HttpSession.class)
				.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_SESSION);
		appComplete.addObject(HttpRequestState.class)
				.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.REQUEST_STATE);
		ManagedFunctionFlowTypeBuilder<CompleteApplicationHttpAuthenticateFunction.Flows> appCompleteFailureFlow = appComplete
				.addFlow();
		appCompleteFailureFlow.setKey(CompleteApplicationHttpAuthenticateFunction.Flows.FAILURE);
		appCompleteFailureFlow.setArgumentType(Throwable.class);
	}

}