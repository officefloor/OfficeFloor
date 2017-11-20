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
package net.officefloor.web.template.section;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.template.section.WebTemplateInitialFunction.Flows;
import net.officefloor.web.template.section.WebTemplateInitialFunction.WebTemplateInitialDependencies;

/**
 * {@link ManagedFunctionSource} to provide the
 * {@link WebTemplateInitialFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateInitialManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Name of the {@link WebTemplateInitialFunction}.
	 */
	public static final String FUNCTION_NAME = "FUNCTION";

	/*
	 * ======================= ManagedFunctionSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Determine if the template is secure
		boolean isSecure = WebTemplateSectionSource.isTemplateSecure(context);

		// Obtain the listing of render redirect HTTP methods
		HttpMethod[] notRedirectHttpMethods = WebTemplateSectionSource.getNotRedirectHttpMethods(context);

		// Obtain the content type and char set
		String contentType = WebTemplateSectionSource.getWebTemplateContentType(context);
		Charset charset = WebTemplateSectionSource.getWebTemplateRenderCharset(context);

		// Create the HTTP Template initial function
		WebTemplateInitialFunction factory = new WebTemplateInitialFunction(isSecure, notRedirectHttpMethods,
				contentType, charset);

		// Configure the function
		ManagedFunctionTypeBuilder<WebTemplateInitialDependencies, Flows> function = namespaceTypeBuilder
				.addManagedFunctionType(FUNCTION_NAME, factory, WebTemplateInitialDependencies.class, Flows.class);
		function.addObject(ServerHttpConnection.class).setKey(WebTemplateInitialDependencies.SERVER_HTTP_CONNECTION);
		function.addFlow().setKey(Flows.RENDER);
		function.addEscalation(IOException.class);
	}

}