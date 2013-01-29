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

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.security.type.HttpSecurityDependencyType;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link SectionSource} for the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySectionSource extends AbstractSectionSource {

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
		String key = context.getSectionLocation();
		HttpSecurityConfiguration<?, ?, ?, ?> configuration = HttpSecurityConfigurator
				.getHttpSecuritySource(key);

		// Obtain the HTTP Security Type
		HttpSecurityType<?, ?, ?, ?> securityType = configuration
				.getHttpSecurityType();

		// Create the dependent objects
		SectionObject serverHttpConnection = designer.addSectionObject(
				"SERVER_HTTP_CONNECTION", ServerHttpConnection.class.getName());
		SectionObject httpSession = designer.addSectionObject("HTTP_SESSION",
				HttpSession.class.getName());
		SectionObject httpRequestState = designer.addSectionObject(
				"HTTP_REQUEST_STATE", HttpRequestState.class.getName());
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

		// Configure the HTTP Security Work Source
		SectionWork work = designer.addSectionWork("HttpSecuritySource",
				HttpSecurityWorkSource.class.getName());
		work.addProperty(
				HttpSecurityWorkSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY, key);

		// Configure the challenge handling
		SectionTask challengeTask = work.addSectionTask("Challenge",
				HttpSecurityWorkSource.TASK_CHALLENGE);
		designer.link(challengeTask.getTaskObject("SERVER_HTTP_CONNECTION"),
				serverHttpConnection);
		designer.link(challengeTask.getTaskObject("HTTP_SESSION"), httpSession);
		designer.link(challengeTask.getTaskObject("HTTP_REQUEST_STATE"),
				httpRequestState);
		for (SectionObject dependency : dependencyObjects) {
			designer.link(challengeTask.getTaskObject(dependency
					.getSectionObjectName()), dependency);
		}

	}

}