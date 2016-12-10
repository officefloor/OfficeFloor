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

import java.util.Map;

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

/**
 * Allows wiring the flows of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpTemplateAutoWireSection extends AutoWireSection {

	/**
	 * Obtains the URI to the template.
	 * 
	 * @return URI to the template.
	 */
	String getTemplateUri();

	/**
	 * Obtains path to the template file.
	 * 
	 * @return Path to the template file.
	 */
	String getTemplatePath();

	/**
	 * Obtains the logic class for the template.
	 * 
	 * @return Logic class for the template.
	 */
	Class<?> getTemplateLogicClass();

	/**
	 * <p>
	 * Specifies the template URI suffix.
	 * <p>
	 * This is appended to the template URI and link URIs. It is useful to
	 * provide a &quot;.extension&quot; to the template URIs.
	 * 
	 * @param uriSuffix
	 *            Template URI suffix. May be <code>null</code> to not have a
	 *            URI suffix.
	 */
	void setTemplateUriSuffix(String uriSuffix);

	/**
	 * Obtains the template URI suffix.
	 * 
	 * @return Template URI suffix. May be <code>null</code> to not have a URI
	 *         suffix.
	 */
	String getTemplateUriSuffix();

	/**
	 * <p>
	 * Specifies the Content-Type for the template.
	 * <p>
	 * Note for <code>text/*</code> values, the <code>charset</code> parameter
	 * will be respected in generating the template.
	 * 
	 * @param contentType
	 *            Content-Type for the template.
	 */
	void setTemplateContentType(String contentType);

	/**
	 * Obtains the Content-Type for the template.
	 * 
	 * @return Content-Type for the template. May be <code>null</code> to use
	 *         the default encoding.
	 */
	String getTemplateContentType();

	/**
	 * <p>
	 * Indicate whether a secure {@link ServerHttpConnection} is required for
	 * the template. This applies to:
	 * <ul>
	 * <li>all links for the {@link HttpTemplateAutoWireSection}</li>
	 * <li>rendering of the {@link HttpTemplateAutoWireSection} response (a
	 * redirect will be triggered if not appropriately secure)</li>
	 * </ul>
	 * 
	 * @param isSecure
	 *            <code>true</code> should the {@link AutoWireSection} require a
	 *            secure {@link ServerHttpConnection}.
	 */
	void setTemplateSecure(boolean isSecure);

	/**
	 * Indicates whether a secure {@link ServerHttpConnection} is required for
	 * the template.
	 * 
	 * @return <code>true</code> if a secure {@link ServerHttpConnection} is
	 *         required for the template.
	 */
	boolean isTemplateSecure();

	/**
	 * <p>
	 * Indicate whether a secure {@link ServerHttpConnection} is required for
	 * the link. This overrides the default template secure setting for the
	 * link.
	 * <p>
	 * Example use could be the landing page may be insecure but the login form
	 * submission link on the page is to be secure.
	 * 
	 * @param linkName
	 *            Name of link to secure.
	 * @param isSecure
	 *            <code>true</code> should the link require a secure
	 *            {@link ServerHttpConnection}.
	 */
	void setLinkSecure(String linkName, boolean isSecure);

	/**
	 * <p>
	 * Obtains an immutable {@link Map} providing the overriding configuration
	 * of whether a link requires a secure {@link ServerHttpConnection}.
	 * <p>
	 * Links not contained in the returned {@link Map} will default secure to
	 * that of the template.
	 * 
	 * @return Immutable {@link Map} of link to whether if requires a secure
	 *         {@link ServerHttpConnection}.
	 */
	Map<String, Boolean> getSecureLinks();

	/**
	 * <p>
	 * Adds a HTTP method that will cause a redirect on rendering this
	 * {@link HttpTemplate}.
	 * <p>
	 * This allows specifying the HTTP methods that will follow the
	 * POST/redirect/GET pattern.
	 * 
	 * @param renderRedirectHttpMethod
	 *            HTTP method.
	 */
	void addRenderRedirectHttpMethod(String renderRedirectHttpMethod);

	/**
	 * Obtains the configured render redirect HTTP methods.
	 * 
	 * @return Configured render redirect HTTP methods.
	 */
	String[] getRenderRedirectHttpMethods();

	/**
	 * Adds an {@link HttpTemplateSectionExtension} to this
	 * {@link HttpTemplateAutoWireSection}.
	 * 
	 * @param extensionClass
	 *            Class of the {@link HttpTemplateSectionExtension}.
	 * @return {@link HttpTemplateAutoWireSectionExtension}.
	 */
	HttpTemplateAutoWireSectionExtension addTemplateExtension(
			Class<? extends HttpTemplateSectionExtension> extensionClass);

}