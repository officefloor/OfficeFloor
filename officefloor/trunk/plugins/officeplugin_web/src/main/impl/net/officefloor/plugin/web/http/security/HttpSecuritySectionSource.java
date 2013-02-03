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

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.security.type.HttpSecurityDependencyType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityFlowType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link SectionSource} for the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySectionSource extends AbstractSectionSource {

	/**
	 * Name of {@link Property} providing the key to the
	 * {@link HttpSecuritySource} from the {@link HttpSecurityConfigurator}.
	 */
	public static final String PROPERTY_HTTP_SECURITY_SOURCE_KEY = HttpAuthenticationManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY;

	/*
	 * ===================== SectionSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Retrieve the HTTP Security configuration
		String key = context.getProperty(PROPERTY_HTTP_SECURITY_SOURCE_KEY);
		HttpSecurityConfiguration<?, ?, ?, ?> configuration = HttpSecurityConfigurator
				.getHttpSecuritySource(key);

		// Obtain the HTTP Security Type
		HttpSecurityType<?, ?, ?, ?> securityType = configuration
				.getHttpSecurityType();

		// Obtain the credentials type
		Class<?> credentialsType = HttpSecurityWorkSource
				.getCredentialsClass(securityType);

		// Create the dependent objects
		SectionObject serverHttpConnection = designer.addSectionObject(
				"SERVER_HTTP_CONNECTION", ServerHttpConnection.class.getName());
		SectionObject httpSession = designer.addSectionObject("HTTP_SESSION",
				HttpSession.class.getName());
		SectionObject httpRequestState = designer.addSectionObject(
				"HTTP_REQUEST_STATE", HttpRequestState.class.getName());
		SectionObject httpAuthentication = designer.addSectionObject(
				"HTTP_AUTHENTICATION", HttpAuthentication.class.getName());
		HttpSecurityDependencyType<?>[] dependencyTypes = securityType
				.getDependencyTypes();
		SectionObject[] dependencyObjects = new SectionObject[dependencyTypes.length];
		for (int i = 0; i < dependencyObjects.length; i++) {
			HttpSecurityDependencyType<?> dependencyType = dependencyTypes[i];
			dependencyObjects[i] = designer.addSectionObject("DEPENDENCY_"
					+ dependencyType.getDependencyName(), dependencyType
					.getDependencyType().getName());
			dependencyObjects[i].setTypeQualifier(dependencyType
					.getTypeQualifier());
		}

		// Create the failure flow
		SectionOutput failureOutput = designer.addSectionOutput("Failure",
				Throwable.class.getName(), true);

		// Configure the HTTP Security Work Source
		SectionWork work = designer.addSectionWork("HttpSecuritySource",
				HttpSecurityWorkSource.class.getName());
		work.addProperty(
				HttpSecurityWorkSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY, key);

		// Configure the challenge handling
		SectionTask challengeTask = work.addSectionTask(
				HttpSecurityWorkSource.TASK_CHALLENGE,
				HttpSecurityWorkSource.TASK_CHALLENGE);
		challengeTask.getTaskObject("HTTP_AUTHENTICATION_REQUIRED_EXCEPTION")
				.flagAsParameter();
		designer.link(challengeTask.getTaskObject("SERVER_HTTP_CONNECTION"),
				serverHttpConnection);
		designer.link(challengeTask.getTaskObject("HTTP_SESSION"), httpSession);
		designer.link(challengeTask.getTaskObject("HTTP_REQUEST_STATE"),
				httpRequestState);
		for (SectionObject dependency : dependencyObjects) {
			designer.link(challengeTask.getTaskObject(dependency
					.getSectionObjectName()), dependency);
		}
		designer.link(challengeTask.getTaskFlow("FAILURE"), failureOutput,
				FlowInstigationStrategyEnum.SEQUENTIAL);
		for (HttpSecurityFlowType<?> flowType : securityType.getFlowTypes()) {
			designer.link(challengeTask.getTaskFlow(flowType.getFlowName()),
					designer.addSectionOutput(flowType.getFlowName(), flowType
							.getArgumentType().getName(), false),
					FlowInstigationStrategyEnum.SEQUENTIAL);
		}

		// Configure the managed object authentication
		SectionTask moAuthTask = work.addSectionTask(
				HttpSecurityWorkSource.TASK_MANAGED_OBJECT_AUTHENTICATE,
				HttpSecurityWorkSource.TASK_MANAGED_OBJECT_AUTHENTICATE);
		moAuthTask.getTaskObject("TASK_AUTHENTICATE_CONTEXT").flagAsParameter();
		for (SectionObject dependency : dependencyObjects) {
			designer.link(
					moAuthTask.getTaskObject(dependency.getSectionObjectName()),
					dependency);
		}

		// Configure the application authentication start
		SectionTask startAuthTask = work.addSectionTask(
				HttpSecurityWorkSource.TASK_START_APPLICATION_AUTHENTICATE,
				HttpSecurityWorkSource.TASK_START_APPLICATION_AUTHENTICATE);
		startAuthTask.getTaskObject(
				StartApplicationHttpAuthenticateTask.Dependencies.CREDENTIALS
						.name()).flagAsParameter();
		designer.link(
				startAuthTask
						.getTaskObject(StartApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION
								.name()), httpAuthentication);
		designer.link(startAuthTask
				.getTaskFlow(StartApplicationHttpAuthenticateTask.Flows.FAILURE
						.name()), failureOutput,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		// Configure the application authentication completion
		SectionTask completeAuthTask = work.addSectionTask(
				HttpSecurityWorkSource.TASK_COMPLETE_APPLICATION_AUTHENTICATE,
				HttpSecurityWorkSource.TASK_COMPLETE_APPLICATION_AUTHENTICATE);
		designer.link(
				completeAuthTask
						.getTaskObject(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_AUTHENTICATION
								.name()), httpAuthentication);
		designer.link(
				completeAuthTask
						.getTaskObject(CompleteApplicationHttpAuthenticateTask.Dependencies.SERVER_HTTP_CONNECTION
								.name()), serverHttpConnection);
		designer.link(
				completeAuthTask
						.getTaskObject(CompleteApplicationHttpAuthenticateTask.Dependencies.HTTP_SESSION
								.name()), httpSession);
		designer.link(
				completeAuthTask
						.getTaskObject(CompleteApplicationHttpAuthenticateTask.Dependencies.REQUEST_STATE
								.name()), httpRequestState);
		designer.link(
				completeAuthTask
						.getTaskFlow(CompleteApplicationHttpAuthenticateTask.Flows.FAILURE
								.name()), failureOutput,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		// Link completion for started authentication
		designer.link(startAuthTask, completeAuthTask);

		// Link re-continue for completed authentication
		designer.link(completeAuthTask,
				designer.addSectionOutput("Recontinue", null, false));

		// Link inputs
		designer.link(designer.addSectionInput("Challenge",
				HttpAuthenticationRequiredException.class.getName()),
				challengeTask);
		designer.link(
				designer.addSectionInput("Authenticate",
						credentialsType.getName()), startAuthTask);
		designer.link(designer.addSectionInput("ManagedObjectAuthenticate",
				TaskAuthenticateContext.class.getName()), moAuthTask);
	}

}