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

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;

/**
 * Allows wiring the flows of the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpTemplateSection {

	/**
	 * Obtains the underlying {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSection}.
	 */
	OfficeSection getOfficeSection();

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
	 * <p>
	 * Indicate whether a secure {@link ServerHttpConnection} is required for
	 * the template. This applies to:
	 * <ul>
	 * <li>all links for the {@link HttpTemplateSection}</li>
	 * <li>rendering of the {@link HttpTemplateSection} response (a redirect
	 * will be triggered if not appropriately secure)</li>
	 * </ul>
	 * 
	 * @param isSecure
	 *            <code>true</code> should this require a secure
	 *            {@link ServerHttpConnection}.
	 */
	void setTemplateSecure(boolean isSecure);

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
	 * Specifies the parent {@link HttpTemplateSection} to inherit from.
	 * 
	 * @param httpTemplateSection
	 *            Parent {@link HttpTemplateSection}.
	 */
	void setSuperHttpTemplate(HttpTemplateSection httpTemplateSection);

	/**
	 * Adds an {@link HttpTemplateSectionExtension} to this
	 * {@link HttpTemplateSection}.
	 * 
	 * @param extensionClass
	 *            Class of the {@link HttpTemplateSectionExtension}.
	 * @return {@link HttpTemplateAutoWireSectionExtension}.
	 */
	HttpTemplateAutoWireSectionExtension addTemplateExtension(
			Class<? extends HttpTemplateSectionExtension> extensionClass);

}