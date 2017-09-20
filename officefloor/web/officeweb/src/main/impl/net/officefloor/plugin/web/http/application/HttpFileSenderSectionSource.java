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
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryFunction.DependencyKeys;
import net.officefloor.plugin.web.http.resource.source.HttpFileSenderManagedFunctionSource;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Provides sending {@link HttpFile} instances for the HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileSenderSectionSource extends AbstractSectionSource {

	/**
	 * Name of the {@link SectionInput} to provide servicing to send the
	 * {@link HttpFile}.
	 */
	public static final String SERVICE_INPUT_NAME = "service";

	/**
	 * Name of the {@link SectionOutput} that is instigated after the
	 * {@link HttpFile} is written to the {@link HttpResponse}.
	 */
	public static final String FILE_SENT_OUTPUT_NAME = "file-sent";

	/*
	 * ====================== SectionSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		final Map<Class<?>, SectionObject> objects = new HashMap<Class<?>, SectionObject>();
		final Map<Class<?>, SectionOutput> escalations = new HashMap<Class<?>, SectionOutput>();

		// Add file sender
		SectionFunctionNamespace sendFileNamespace = designer.addSectionFunctionNamespace("FILE",
				HttpFileSenderManagedFunctionSource.class.getName());
		SourceHttpResourceFactory.copyProperties(context, sendFileNamespace);
		SectionFunction sendFileFunction = sendFileNamespace.addSectionFunction(
				HttpFileSenderManagedFunctionSource.FUNCTION_NAME, HttpFileSenderManagedFunctionSource.FUNCTION_NAME);
		WebApplicationSectionSource.linkObject(sendFileFunction, DependencyKeys.SERVER_HTTP_CONNECTION.name(),
				ServerHttpConnection.class, designer, objects);
		WebApplicationSectionSource.linkObject(sendFileFunction, DependencyKeys.HTTP_APPLICATION_LOCATION.name(),
				HttpApplicationLocation.class, designer, objects);
		WebApplicationSectionSource.linkEscalation(sendFileFunction, IOException.class, designer, escalations);

		// Provide input to service
		SectionInput input = designer.addSectionInput(SERVICE_INPUT_NAME, null);
		designer.link(input, sendFileFunction);

		// Provide output once file written
		SectionOutput output = designer.addSectionOutput(FILE_SENT_OUTPUT_NAME, null, false);
		designer.link(sendFileFunction, output);
	}

}