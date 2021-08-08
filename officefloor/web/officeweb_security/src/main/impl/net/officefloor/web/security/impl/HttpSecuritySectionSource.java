/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.LogoutRequest;
import net.officefloor.web.security.type.HttpSecurityDependencyType;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityExecuteContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link SectionSource} for the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySectionSource<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
		extends AbstractSectionSource {

	/**
	 * Name of the {@link SectionInput} for challenging.
	 */
	public static final String INPUT_CHALLENGE = "Challenge";

	/**
	 * Name of the {@link SectionInput} for undertaking authentication with
	 * application provided credentials.
	 */
	public static final String INPUT_AUTHENTICATE = "Authenticate";

	/**
	 * Name of the {@link SectionOutput} for handling re-continuing after
	 * authentication.
	 */
	public static final String OUTPUT_RECONTINUE = "Recontinue";

	/**
	 * Prefix name of the {@link SectionInput} for handling
	 * {@link HttpSecurityExecuteContext}.
	 */
	public static final String INPUT_FLOW_PREFIX = "Input_";

	/**
	 * {@link HttpSecurityConfiguration}.
	 */
	private final HttpSecurityConfiguration<A, AC, C, O, F> configuration;

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link HttpSecurityConfiguration}.
	 */
	public HttpSecuritySectionSource(HttpSecurityConfiguration<A, AC, C, O, F> configuration) {
		this.configuration = configuration;
	}

	/*
	 * ===================== SectionSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain the security and it's type
		String securityName = this.configuration.getHttpSecurityName();
		HttpSecurity<A, AC, C, O, F> security = this.configuration.getHttpSecurity();
		HttpSecurityType<A, AC, C, O, F> securityType = this.configuration.getHttpSecurityType();

		// Obtain the type details
		final Class<AC> accessControlType = securityType.getAccessControlType();
		final Class<C> credentialsType = securityType.getCredentialsType();

		// Create the dependent objects (from web application)
		SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
				ServerHttpConnection.class.getName());
		SectionObject httpSession = designer.addSectionObject(HttpSession.class.getSimpleName(),
				HttpSession.class.getName());
		SectionObject httpRequestState = designer.addSectionObject(HttpRequestState.class.getSimpleName(),
				HttpRequestState.class.getName());

		// Create the authentication dependencies
		SectionObject authenticationContext = designer.addSectionObject(AuthenticationContext.class.getSimpleName(),
				AuthenticationContext.class.getName());
		SectionObject accessControl = designer.addSectionObject("AccessControl", accessControlType.getName());
		SectionObject httpChallengeContext = designer.addSectionObject(HttpChallengeContext.class.getSimpleName(),
				HttpChallengeContext.class.getName());

		// Load the dynamic dependencies
		HttpSecurityDependencyType<?>[] dependencyTypes = securityType.getDependencyTypes();
		SectionObject[] dependencyObjects = new SectionObject[dependencyTypes.length];
		for (int i = 0; i < dependencyObjects.length; i++) {
			HttpSecurityDependencyType<?> dependencyType = dependencyTypes[i];
			dependencyObjects[i] = designer.addSectionObject("Dependency_" + dependencyType.getDependencyName(),
					dependencyType.getDependencyType().getName());
			dependencyObjects[i].setTypeQualifier(dependencyType.getTypeQualifier());
		}

		// Links the dynamic dependencies
		Consumer<SectionFunction> dynamicDependencyLinker = (function) -> {
			for (SectionObject dependency : dependencyObjects) {
				designer.link(function.getFunctionObject(dependency.getSectionObjectName()), dependency);
			}
		};

		// Link input flow handling to output flows
		Map<String, SectionOutput> sectionOutputs = new HashMap<>();
		for (HttpSecurityFlowType<?> flowType : securityType.getFlowTypes()) {
			String flowName = flowType.getFlowName();

			// Create and register the section outputs for flows
			Class<?> argumentType = flowType.getArgumentType();
			String argumentTypeName = argumentType == null ? null : argumentType.getName();
			SectionOutput sectionOutput = designer.addSectionOutput(flowName, argumentTypeName, false);
			sectionOutputs.put(flowName, sectionOutput);

			// Create and link the section inputs
			SectionInput sectionInput = designer.addSectionInput(INPUT_FLOW_PREFIX + flowName, argumentTypeName);
			designer.link(sectionInput, sectionOutput);
		}

		// Link the flow handling
		Consumer<SectionFunction> flowsLinker = (function) -> {
			for (HttpSecurityFlowType<?> flowType : securityType.getFlowTypes()) {
				String flowName = flowType.getFlowName();
				FunctionFlow functionFlow = function.getFunctionFlow("Flow_" + flowName);
				SectionOutput sectionOutput = sectionOutputs.get(flowName);
				designer.link(functionFlow, sectionOutput, false);
			}
		};

		// Configure the HTTP Security Managed Function Source
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("HttpSecuritySource",
				new HttpSecurityManagedFunctionSource(securityName, security, securityType));

		// Configure the challenge handling
		SectionFunction challengeFunction = namespace.addSectionFunction(
				HttpSecurityManagedFunctionSource.FUNCTION_CHALLENGE,
				HttpSecurityManagedFunctionSource.FUNCTION_CHALLENGE);
		designer.link(challengeFunction.getFunctionObject(HttpChallengeContext.class.getSimpleName()),
				httpChallengeContext);
		designer.link(challengeFunction.getFunctionObject(ServerHttpConnection.class.getSimpleName()),
				serverHttpConnection);
		designer.link(challengeFunction.getFunctionObject(HttpSession.class.getSimpleName()), httpSession);
		designer.link(challengeFunction.getFunctionObject(HttpRequestState.class.getSimpleName()), httpRequestState);
		dynamicDependencyLinker.accept(challengeFunction);
		flowsLinker.accept(challengeFunction);

		// Configure the managed object authentication
		SectionFunction moAuthFunction = namespace.addSectionFunction(
				HttpSecurityManagedFunctionSource.FUNCTION_MANAGED_OBJECT_AUTHENTICATE,
				HttpSecurityManagedFunctionSource.FUNCTION_MANAGED_OBJECT_AUTHENTICATE);
		moAuthFunction.getFunctionObject(FunctionAuthenticateContext.class.getSimpleName()).flagAsParameter();
		dynamicDependencyLinker.accept(moAuthFunction);
		flowsLinker.accept(moAuthFunction);

		// Configure the managed object logout
		SectionFunction moLogoutFunction = namespace.addSectionFunction(
				HttpSecurityManagedFunctionSource.FUNCTION_MANAGED_OBJECT_LOGOUT,
				HttpSecurityManagedFunctionSource.FUNCTION_MANAGED_OBJECT_LOGOUT);
		moLogoutFunction.getFunctionObject(FunctionLogoutContext.class.getSimpleName()).flagAsParameter();
		dynamicDependencyLinker.accept(moLogoutFunction);
		flowsLinker.accept(moLogoutFunction);

		// Determine if application credential login
		if (credentialsType != null) {

			// Configure the application authentication start
			SectionFunction startAuthFunction = namespace.addSectionFunction(
					HttpSecurityManagedFunctionSource.FUNCTION_START_APPLICATION_AUTHENTICATE,
					HttpSecurityManagedFunctionSource.FUNCTION_START_APPLICATION_AUTHENTICATE);
			startAuthFunction
					.getFunctionObject(StartApplicationHttpAuthenticateFunction.Dependencies.CREDENTIALS.name())
					.flagAsParameter();
			designer.link(
					startAuthFunction.getFunctionObject(
							StartApplicationHttpAuthenticateFunction.Dependencies.AUTHENTICATION_CONTEXT.name()),
					authenticationContext);

			// Configure the application authentication completion
			SectionFunction completeAuthFunction = namespace.addSectionFunction(
					HttpSecurityManagedFunctionSource.FUNCTION_COMPLETE_APPLICATION_AUTHENTICATE,
					HttpSecurityManagedFunctionSource.FUNCTION_COMPLETE_APPLICATION_AUTHENTICATE);
			designer.link(
					completeAuthFunction.getFunctionObject(
							CompleteApplicationHttpAuthenticateFunction.Dependencies.ACCESS_CONTROL.name()),
					accessControl);
			designer.link(
					completeAuthFunction.getFunctionObject(
							CompleteApplicationHttpAuthenticateFunction.Dependencies.SERVER_HTTP_CONNECTION.name()),
					serverHttpConnection);
			designer.link(completeAuthFunction.getFunctionObject(
					CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_SESSION.name()), httpSession);
			designer.link(
					completeAuthFunction.getFunctionObject(
							CompleteApplicationHttpAuthenticateFunction.Dependencies.REQUEST_STATE.name()),
					httpRequestState);

			// Link completion for started authentication
			designer.link(startAuthFunction, completeAuthFunction);

			// Link re-continue for completed authentication
			designer.link(completeAuthFunction, designer.addSectionOutput(OUTPUT_RECONTINUE, null, false));

			// Link input for application login
			designer.link(designer.addSectionInput(INPUT_AUTHENTICATE, credentialsType.getName()), startAuthFunction);
		}

		// Link inputs
		designer.link(designer.addSectionInput(INPUT_CHALLENGE, AuthenticationRequiredException.class.getName()),
				challengeFunction);
		designer.link(
				designer.addSectionInput("ManagedObjectAuthenticate", FunctionAuthenticateContext.class.getName()),
				moAuthFunction);
		designer.link(designer.addSectionInput("ManagedObjectLogout", LogoutRequest.class.getName()), moLogoutFunction);
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link HttpSecurity}
	 * {@link ManagedFunction} instances.
	 */
	private class HttpSecurityManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link HttpChallengeFunction}.
		 */
		private static final String FUNCTION_CHALLENGE = "Challenge";

		/**
		 * Name of the {@link ManagedObjectAuthenticateFunction}.
		 */
		private static final String FUNCTION_MANAGED_OBJECT_AUTHENTICATE = "ManagedObjectAuthenticate";

		/**
		 * Name of the {@link ManagedObjectLogoutFunction}.
		 */
		private static final String FUNCTION_MANAGED_OBJECT_LOGOUT = "ManagedObjectLogout";

		/**
		 * Name of the {@link StartApplicationHttpAuthenticateFunction}.
		 */
		private static final String FUNCTION_START_APPLICATION_AUTHENTICATE = "StartApplicationAuthenticate";

		/**
		 * Name of the {@link CompleteApplicationHttpAuthenticateFunction}.
		 */
		private static final String FUNCTION_COMPLETE_APPLICATION_AUTHENTICATE = "CompleteApplicationAuthenticate";

		/**
		 * Name of the {@link HttpSecurity}.
		 */
		private final String httpSecurityName;

		/**
		 * {@link HttpSecurity}.
		 */
		private final HttpSecurity<A, AC, C, O, F> httpSecurity;

		/**
		 * {@link HttpSecurityType}.
		 */
		private final HttpSecurityType<A, AC, C, O, F> httpSecurityType;

		/**
		 * Instantiate.
		 * 
		 * @param httpSecurityName Name of the {@link HttpSecurity}.
		 * @param httpSecurity     {@link HttpSecurity}.
		 * @param httpSecurityType {@link HttpSecurityType}.
		 */
		public HttpSecurityManagedFunctionSource(String httpSecurityName, HttpSecurity<A, AC, C, O, F> httpSecurity,
				HttpSecurityType<A, AC, C, O, F> httpSecurityType) {
			this.httpSecurityName = httpSecurityName;
			this.httpSecurity = httpSecurity;
			this.httpSecurityType = httpSecurityType;
		}

		/*
		 * =================== ManagedFunctionSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the type information
			Class<AC> accessControlType = this.httpSecurityType.getAccessControlType();
			Class<C> credentialsType = this.httpSecurityType.getCredentialsType();

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
			Class<F> flowKeys = HttpSecuritySectionSource.this.configuration.getFlowKeyClass();
			List<HttpSecurityFlowType<F>> flowTypes = new ArrayList<>(Arrays.asList(httpSecurityType.getFlowTypes()));
			Collections.sort(flowTypes, new Comparator<HttpSecurityFlowType<?>>() {
				@Override
				public int compare(HttpSecurityFlowType<?> a, HttpSecurityFlowType<?> b) {
					return a.getIndex() - b.getIndex();
				}
			});

			// Dependency and flow loader
			Consumer<ManagedFunctionTypeBuilder<Indexed, F>> dependencyLoader = (function) -> {
				for (HttpSecurityDependencyType<?> dependencyType : dependencyTypes) {
					function.addObject(dependencyType.getDependencyType())
							.setLabel("Dependency_" + dependencyType.getDependencyName());
				}
			};
			Consumer<ManagedFunctionTypeBuilder<Indexed, F>> flowLoader = (function) -> {
				for (HttpSecurityFlowType<F> flowType : flowTypes) {
					ManagedFunctionFlowTypeBuilder<F> flow = function.addFlow();
					flow.setKey(flowType.getKey());
					flow.setArgumentType(flowType.getArgumentType());
					flow.setLabel("Flow_" + flowType.getFlowName());
				}
			};

			// Add the managed object authentication function
			ManagedFunctionTypeBuilder<Indexed, F> moAuthenticate = namespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_MANAGED_OBJECT_AUTHENTICATE, Indexed.class, flowKeys)
					.setFunctionFactory(
							new ManagedObjectAuthenticateFunction<>(this.httpSecurityName, this.httpSecurity));
			moAuthenticate.addObject(FunctionAuthenticateContext.class)
					.setLabel(FunctionAuthenticateContext.class.getSimpleName());
			dependencyLoader.accept(moAuthenticate);
			flowLoader.accept(moAuthenticate);

			// Add the managed object logout function
			ManagedFunctionTypeBuilder<Indexed, F> logout = namespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_MANAGED_OBJECT_LOGOUT, Indexed.class, flowKeys)
					.setFunctionFactory(new ManagedObjectLogoutFunction<>(this.httpSecurityName, this.httpSecurity));
			logout.addObject(FunctionLogoutContext.class).setLabel(FunctionLogoutContext.class.getSimpleName());
			dependencyLoader.accept(logout);
			flowLoader.accept(logout);

			// Add the challenge function
			ManagedFunctionTypeBuilder<Indexed, F> challenge = namespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_CHALLENGE, Indexed.class, flowKeys)
					.setFunctionFactory(new HttpChallengeFunction<>(this.httpSecurityName, this.httpSecurity));
			challenge.addObject(HttpChallengeContext.class).setLabel(HttpChallengeContext.class.getSimpleName());
			challenge.addObject(ServerHttpConnection.class).setLabel(ServerHttpConnection.class.getSimpleName());
			challenge.addObject(HttpSession.class).setLabel(HttpSession.class.getSimpleName());
			challenge.addObject(HttpRequestState.class).setLabel(HttpRequestState.class.getSimpleName());
			dependencyLoader.accept(challenge);
			flowLoader.accept(challenge);

			// Add the start application authentication function
			if (credentialsType != null) {
				ManagedFunctionTypeBuilder<StartApplicationHttpAuthenticateFunction.Dependencies, None> appStart = namespaceTypeBuilder
						.addManagedFunctionType(FUNCTION_START_APPLICATION_AUTHENTICATE,
								StartApplicationHttpAuthenticateFunction.Dependencies.class, None.class)
						.setFunctionFactory(new StartApplicationHttpAuthenticateFunction<>());
				appStart.addObject(AuthenticationContext.class)
						.setKey(StartApplicationHttpAuthenticateFunction.Dependencies.AUTHENTICATION_CONTEXT);
				appStart.addObject(credentialsType)
						.setKey(StartApplicationHttpAuthenticateFunction.Dependencies.CREDENTIALS);
			}

			// Add the complete application authentication function
			ManagedFunctionTypeBuilder<CompleteApplicationHttpAuthenticateFunction.Dependencies, None> appComplete = namespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_COMPLETE_APPLICATION_AUTHENTICATE,
							CompleteApplicationHttpAuthenticateFunction.Dependencies.class, None.class)
					.setFunctionFactory(new CompleteApplicationHttpAuthenticateFunction<>());
			appComplete.addObject(accessControlType)
					.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.ACCESS_CONTROL);
			appComplete.addObject(ServerHttpConnection.class)
					.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.SERVER_HTTP_CONNECTION);
			appComplete.addObject(HttpSession.class)
					.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.HTTP_SESSION);
			appComplete.addObject(HttpRequestState.class)
					.setKey(CompleteApplicationHttpAuthenticateFunction.Dependencies.REQUEST_STATE);
		}
	}

}
