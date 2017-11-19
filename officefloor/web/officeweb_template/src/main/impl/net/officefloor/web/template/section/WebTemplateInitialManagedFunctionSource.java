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

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.template.WebTemplateManagedFunctionSource;
import net.officefloor.web.template.parse.ParsedTemplate;
import net.officefloor.web.template.section.WebTemplateInitialFunction.Dependencies;
import net.officefloor.web.template.section.WebTemplateInitialFunction.Flows;

/**
 * {@link ManagedFunctionSource} to provide the
 * {@link WebTemplateInitialFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateInitialManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Property name for a comma separated list of HTTP methods that will
	 * trigger a redirect before rendering the {@link ParsedTemplate}.
	 */
	public static final String PROPERTY_RENDER_REDIRECT_HTTP_METHODS = "http.template.render.redirect.methods";

	/**
	 * Property name for the Content-Type of the {@link ParsedTemplate}.
	 */
	public static final String PROPERTY_CONTENT_TYPE = "http.template.render.content.type";

	/**
	 * Property name for the {@link Charset} of the {@link ParsedTemplate}.
	 */
	public static final String PROPERTY_CHARSET = "http.template.charset";

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
		boolean isSecure = WebTemplateManagedFunctionSource.isWebTemplateSecure(context);

		// Obtain the listing of render redirect HTTP methods
		HttpMethod[] notRedirectHttpMethods = null;
		String notRedirectProperty = context.getProperty(PROPERTY_RENDER_REDIRECT_HTTP_METHODS, null);
		if (notRedirectProperty != null) {
			// Obtain the render redirect HTTP methods
			String[] notRedirectHttpMethodNames = notRedirectProperty.split(",");
			notRedirectHttpMethods = new HttpMethod[notRedirectHttpMethodNames.length];
			for (int i = 0; i < notRedirectHttpMethodNames.length; i++) {
				notRedirectHttpMethods[i] = HttpMethod.getHttpMethod(notRedirectHttpMethodNames[i].trim());
			}
		}

		// Obtain the content type and charset
		String contentType = context.getProperty(PROPERTY_CONTENT_TYPE, null);
		Charset charset = null;
		String charsetName = context.getProperty(PROPERTY_CHARSET);
		if (!CompileUtil.isBlank(charsetName)) {
			charset = Charset.forName(charsetName);
		}

		// Create the HTTP Template initial function
		WebTemplateInitialFunction factory = new WebTemplateInitialFunction(isSecure, notRedirectHttpMethods,
				contentType, charset);

		// Configure the function
		ManagedFunctionTypeBuilder<Dependencies, Flows> function = namespaceTypeBuilder.addManagedFunctionType("TASK",
				factory, Dependencies.class, Flows.class);
		function.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
		function.addFlow().setKey(Flows.RENDER);
		function.addEscalation(IOException.class);
	}

}