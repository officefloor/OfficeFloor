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

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
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
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.impl.HandleAuthenticationRequiredFunction.Dependencies;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpChallengeContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link SectionSource} to handle the {@link AuthenticationRequiredException}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HandleAuthenticationRequiredSectionSource extends AbstractSectionSource {

	/**
	 * Name of {@link SectionInput} to handle the
	 * {@link AuthenticationRequiredException}.
	 */
	public static final String HANDLE_INPUT = "handle";

	/**
	 * Names of the {@link HttpSecurity} instances.
	 */
	private final String[] httpSecurityNames;

	/**
	 * Challenge {@link AcceptNegotiator}.
	 */
	private final AcceptNegotiator<int[]> challengeNegotiator;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityNames   Names of the {@link HttpSecurity} instances.
	 * @param challengeNegotiator Challenge {@link AcceptNegotiator}.
	 */
	public HandleAuthenticationRequiredSectionSource(String[] httpSecurityNames,
			AcceptNegotiator<int[]> challengeNegotiator) {
		this.httpSecurityNames = httpSecurityNames;
		this.challengeNegotiator = challengeNegotiator;
	}

	/*
	 * =================== SectionSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Add the input handle
		SectionInput handle = designer.addSectionInput(HANDLE_INPUT, AuthenticationRequiredException.class.getName());

		// Add the dependencies
		SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
				ServerHttpConnection.class.getName());
		SectionObject httpSession = designer.addSectionObject(HttpSession.class.getSimpleName(),
				HttpSession.class.getName());
		SectionObject requestState = designer.addSectionObject(HttpRequestState.class.getSimpleName(),
				HttpRequestState.class.getName());
		SectionObject httpChallengeContext = designer.addSectionObject(HttpChallengeContext.class.getSimpleName(),
				HttpChallengeContext.class.getName());

		// Add function to handle authentication required
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("handle",
				new HandleAuthenticationRequiredManagedFunctionSource());
		SectionFunction handler = namespace.addSectionFunction("handler", "handler");
		handler.getFunctionObject(Dependencies.AUTHENTICATION_REQUIRED_EXCEPTION.name()).flagAsParameter();
		designer.link(handler.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()), serverHttpConnection);
		designer.link(handler.getFunctionObject(Dependencies.HTTP_SESSION.name()), httpSession);
		designer.link(handler.getFunctionObject(Dependencies.REQUEST_STATE.name()), requestState);
		for (String httpSecurityName : this.httpSecurityNames) {

			// Link flow to section output by security name
			SectionOutput output = designer.addSectionOutput(httpSecurityName, null, false);
			FunctionFlow flow = handler.getFunctionFlow(httpSecurityName);
			designer.link(flow, output, false);
		}

		// Add function to send challenge
		SectionFunction sender = namespace.addSectionFunction("sender", "sender");
		designer.link(sender.getFunctionObject(
				net.officefloor.web.security.impl.SendHttpChallengeFunction.Dependencies.HTTP_CHALLENGE_CONTEXT.name()),
				httpChallengeContext);
		designer.link(sender.getFunctionObject(
				net.officefloor.web.security.impl.SendHttpChallengeFunction.Dependencies.SERVER_HTTP_CONNECTION.name()),
				serverHttpConnection);

		// Configure flow to send
		designer.link(handler.getFunctionFlow("Send"), sender, false);

		// Handle the input
		designer.link(handle, handler);
	}

	/**
	 * {@link ManagedFunctionSource} to handle the
	 * {@link AuthenticationRequiredException}.
	 */
	@PrivateSource
	private class HandleAuthenticationRequiredManagedFunctionSource extends AbstractManagedFunctionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Configure the handle function
			HandleAuthenticationRequiredFunction handleFactory = new HandleAuthenticationRequiredFunction(
					HandleAuthenticationRequiredSectionSource.this.httpSecurityNames,
					HandleAuthenticationRequiredSectionSource.this.challengeNegotiator);
			ManagedFunctionTypeBuilder<Dependencies, Indexed> handleFunction = functionNamespaceTypeBuilder
					.addManagedFunctionType("handler", Dependencies.class, Indexed.class)
					.setFunctionFactory(handleFactory);
			handleFunction.addObject(AuthenticationRequiredException.class)
					.setKey(Dependencies.AUTHENTICATION_REQUIRED_EXCEPTION);
			handleFunction.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
			handleFunction.addObject(HttpSession.class).setKey(Dependencies.HTTP_SESSION);
			handleFunction.addObject(HttpRequestState.class).setKey(Dependencies.REQUEST_STATE);
			for (String httpSecurityName : HandleAuthenticationRequiredSectionSource.this.httpSecurityNames) {
				handleFunction.addFlow().setLabel(httpSecurityName);
			}
			handleFunction.addFlow().setLabel("Send");

			// Configure the send function
			SendHttpChallengeFunction sendFactory = new SendHttpChallengeFunction();
			ManagedFunctionTypeBuilder<net.officefloor.web.security.impl.SendHttpChallengeFunction.Dependencies, None> sendFunction = functionNamespaceTypeBuilder
					.addManagedFunctionType("sender",
							net.officefloor.web.security.impl.SendHttpChallengeFunction.Dependencies.class, None.class)
					.setFunctionFactory(sendFactory);
			sendFunction.addObject(HttpChallengeContext.class).setKey(
					net.officefloor.web.security.impl.SendHttpChallengeFunction.Dependencies.HTTP_CHALLENGE_CONTEXT);
			sendFunction.addObject(ServerHttpConnection.class).setKey(
					net.officefloor.web.security.impl.SendHttpChallengeFunction.Dependencies.SERVER_HTTP_CONNECTION);
		}
	}

}
