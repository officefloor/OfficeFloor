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
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.security.impl.HandleAuthenticationRequiredFunction.Dependencies;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link SectionSource} to handle the {@link AuthenticationRequiredException}.
 * 
 * @author Daniel Sagenschneider
 */
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
	 * @param httpSecurityNames
	 *            Names of the {@link HttpSecurity} instances.
	 * @param challengeNegotiator
	 *            Challenge {@link AcceptNegotiator}.
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

		// Add the Server HTTP connection object
		SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
				ServerHttpConnection.class.getName());

		// Add function to handle authentication required
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("handle",
				new HandleAuthenticationRequiredManagedFunctionSource());
		SectionFunction handler = namespace.addSectionFunction("handler", "handler");
		handler.getFunctionObject(Dependencies.AUTHENTICATION_REQUIRED_EXCEPTION.name()).flagAsParameter();
		designer.link(handler.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()), serverHttpConnection);
		for (String httpSecurityName : this.httpSecurityNames) {

			// Link flow to section output by security name
			SectionOutput output = designer.addSectionOutput(httpSecurityName, null, false);
			FunctionFlow flow = handler.getFunctionFlow(httpSecurityName);
			designer.link(flow, output, false);
		}

		// Handle the input
		designer.link(handle, handler);
	}

	/**
	 * {@link ManagedFunctionSource} to handle the
	 * {@link AuthenticationRequiredException}.
	 */
	private class HandleAuthenticationRequiredManagedFunctionSource extends AbstractManagedFunctionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Create the function
			HandleAuthenticationRequiredFunction factory = new HandleAuthenticationRequiredFunction(
					HandleAuthenticationRequiredSectionSource.this.httpSecurityNames,
					HandleAuthenticationRequiredSectionSource.this.challengeNegotiator);

			// Configure the function
			ManagedFunctionTypeBuilder<Dependencies, Indexed> function = functionNamespaceTypeBuilder
					.addManagedFunctionType("handler", factory, Dependencies.class, Indexed.class);
			function.addObject(AuthenticationRequiredException.class)
					.setKey(Dependencies.AUTHENTICATION_REQUIRED_EXCEPTION);
			function.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
			for (String httpSecurityName : HandleAuthenticationRequiredSectionSource.this.httpSecurityNames) {
				function.addFlow().setLabel(httpSecurityName);
			}
		}
	}

}