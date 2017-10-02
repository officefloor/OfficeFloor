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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationAnnotationImpl;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.HttpTemplateManagedFunctionSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialFunction.Dependencies;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialFunction.Flows;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link ManagedFunctionSource} to provide the
 * {@link HttpTemplateInitialFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Property name for the {@link HttpTemplate} URI path.
	 */
	public static final String PROPERTY_TEMPLATE_URI = HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI;

	/**
	 * Property name for a comma separated list of HTTP methods that will
	 * trigger a redirect before rendering the {@link HttpTemplate}.
	 */
	public static final String PROPERTY_RENDER_REDIRECT_HTTP_METHODS = "http.template.render.redirect.methods";

	/**
	 * Property name for the Content-Type of the {@link HttpTemplate}.
	 */
	public static final String PROPERTY_CONTENT_TYPE = "http.template.render.content.type";

	/**
	 * Property name for the {@link Charset} of the {@link HttpTemplate}.
	 */
	public static final String PROPERTY_CHARSET = "http.template.charset";

	/**
	 * Name of the {@link HttpTemplateInitialFunction}.
	 */
	public static final String FUNCTION_NAME = "FUNCTION";

	/*
	 * ======================= ManagedFunctionSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_URI, "URI Path");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the template URI path
		String templateUriPath = HttpTemplateManagedFunctionSource.getHttpTemplateUrlContinuationPath(context);

		// Determine if the template is secure
		boolean isSecure = HttpTemplateManagedFunctionSource.isHttpTemplateSecure(context);

		/*
		 * Only trigger redirect if not secure. If sent on secure connection but
		 * no need for secure, service anyway. This is to save establishing a
		 * new connection and a round trip when already have the request.
		 */
		Boolean isRequireSecure = (isSecure ? Boolean.TRUE : null);

		// Obtain the listing of render redirect HTTP methods
		String[] renderRedirectHttpMethods = null;
		String renderRedirectProperty = context.getProperty(PROPERTY_RENDER_REDIRECT_HTTP_METHODS, null);
		if (renderRedirectProperty != null) {
			// Obtain the render redirect HTTP methods
			renderRedirectHttpMethods = renderRedirectProperty.split(",");
			for (int i = 0; i < renderRedirectHttpMethods.length; i++) {
				renderRedirectHttpMethods[i] = renderRedirectHttpMethods[i].trim();
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
		HttpTemplateInitialFunction factory = new HttpTemplateInitialFunction(templateUriPath, isSecure,
				renderRedirectHttpMethods, contentType, charset);

		// Configure the function
		ManagedFunctionTypeBuilder<Dependencies, Flows> function = namespaceTypeBuilder.addManagedFunctionType("TASK",
				factory, Dependencies.class, Flows.class);
		function.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
		function.addObject(HttpApplicationLocation.class).setKey(Dependencies.HTTP_APPLICATION_LOCATION);
		function.addObject(HttpRequestState.class).setKey(Dependencies.REQUEST_STATE);
		function.addObject(HttpSession.class).setKey(Dependencies.HTTP_SESSION);
		function.addFlow().setKey(Flows.RENDER);
		function.addEscalation(IOException.class);
		function.setDifferentiator(new HttpUrlContinuationAnnotationImpl(templateUriPath, isRequireSecure));
	}

}