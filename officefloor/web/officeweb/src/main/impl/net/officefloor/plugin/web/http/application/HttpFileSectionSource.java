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
package net.officefloor.plugin.web.http.application;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.source.HttpFileManagedFunctionSource;
import net.officefloor.plugin.web.http.resource.source.HttpFileManagedFunctionSource.DependencyKeys;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;

/**
 * Provides sending a particular {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileSectionSource extends AbstractSectionSource {

	/**
	 * Prefix on properties specifying resource paths.
	 */
	public static final String PROPERTY_RESOURCE_PREFIX = "resource_";

	/*
	 * ========================= SectionSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Create the Server HTTP Connection dependency
		SectionObject serverHttpConnectionDependency = designer
				.addSectionObject(ServerHttpConnection.class.getSimpleName(), ServerHttpConnection.class.getName());

		// Create the I/O escalation
		SectionOutput ioEscalationOutput = designer.addSectionOutput(IOException.class.getSimpleName(),
				IOException.class.getName(), true);

		// Link to the resource paths
		Set<String> resourcePaths = new HashSet<String>();
		for (String name : context.getPropertyNames()) {
			if (name.startsWith(PROPERTY_RESOURCE_PREFIX)) {

				// Obtain the resource path
				String resourcePath = context.getProperty(name);

				// Ensure register the resource path only
				if (resourcePaths.contains(resourcePath)) {
					continue; // only register once
				}

				// Create the resource function
				SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(resourcePath,
						HttpFileManagedFunctionSource.class.getName());
				SourceHttpResourceFactory.copyProperties(context, namespace);
				namespace.addProperty(HttpFileManagedFunctionSource.PROPERTY_RESOURCE_PATH, resourcePath);
				SectionFunction function = namespace.addSectionFunction(resourcePath,
						HttpFileManagedFunctionSource.FUNCTION_HTTP_FILE);

				// Link Server HTTP Connection
				FunctionObject serverHttpConnectionObject = function
						.getFunctionObject(DependencyKeys.SERVER_HTTP_CONNECTION.name());
				designer.link(serverHttpConnectionObject, serverHttpConnectionDependency);

				// Link I/O escalation
				FunctionFlow ioEscalationFlow = function.getFunctionEscalation(IOException.class.getName());
				designer.link(ioEscalationFlow, ioEscalationOutput, false);

				// Link input for the resource task
				SectionInput input = designer.addSectionInput(resourcePath, null);
				designer.link(input, function);
			}
		}
	}

}