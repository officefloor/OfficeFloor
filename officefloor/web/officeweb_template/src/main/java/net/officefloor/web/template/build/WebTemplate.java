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
package net.officefloor.web.template.build;

import java.nio.charset.Charset;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.template.extension.WebTemplateExtension;

/**
 * Web template.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplate extends PropertyConfigurable {

	/**
	 * Specifies the logic {@link Class}.
	 * 
	 * @param logicClass
	 *            Logic {@link Class}.
	 */
	void setLogicClass(Class<?> logicClass);

	/**
	 * Specifies the <code>Content-Type</code> output by this
	 * {@link WebTemplate}.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code> output by this {@link WebTemplate}.
	 */
	void setContentType(String contentType);

	/**
	 * Allow overriding the default {@link Charset} to render the
	 * {@link WebTemplate}.
	 * 
	 * @param charset
	 *            {@link Charset} to render the {@link WebTemplate}.
	 */
	void setCharset(Charset charset);

	/**
	 * Flags whether the {@link WebTemplate} may only be rendered over a secure
	 * connection.
	 * 
	 * @param isSecure
	 *            <code>true</code> to only render the {@link WebTemplate} over
	 *            a secure connection.
	 */
	void setSecure(boolean isSecure);

	/**
	 * <p>
	 * Indicate whether a secure connection is required for the link. This
	 * overrides the default template secure setting for the link.
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
	 * Adds a {@link HttpMethod} that will not trigger a redirect on rendering
	 * the {@link WebTemplate}.
	 * <p>
	 * Note that {@link HttpMethod#GET} is always a non-redirect.
	 * 
	 * @param method
	 *            {@link HttpMethod} that will not trigger a redirect on
	 *            rendering the {@link WebTemplate}.
	 */
	void addNonRedirectMethod(HttpMethod method);

	/**
	 * Specifies the super (parent) {@link WebTemplate}.
	 * 
	 * @param superTemplate
	 *            Super {@link WebTemplate}.
	 */
	void setSuperTemplate(WebTemplate superTemplate);

	/**
	 * Adds a {@link WebTemplateExtension} for this {@link WebTemplate}.
	 * 
	 * @param extension
	 *            {@link WebTemplateExtension} for this {@link WebTemplate}.
	 */
	void addExtension(WebTemplateExtension extension);

	/**
	 * Obtains the {@link OfficeSectionInput} to link to this
	 * {@link WebTemplate}.
	 * 
	 * @param valuesType
	 *            Type provided as a parameter to the {@link OfficeSectionInput}
	 *            should the path parameters require being obtained. The type
	 *            should provide a bean property for each path parameter for the
	 *            {@link WebTemplate}. May be <code>null</code> if no path
	 *            parameters are required.
	 * @return {@link OfficeSectionInput} to link to this {@link WebTemplate}.
	 */
	OfficeSectionInput getInput(Class<?> valuesType);

	/**
	 * Obtains the {@link OfficeSectionOutput} from the {@link WebTemplate}.
	 * 
	 * @param outputName
	 *            {@link OfficeSectionOutput} name.
	 * @return {@link OfficeSectionOutput} for the name.
	 */
	OfficeSectionOutput getOutput(String outputName);

}