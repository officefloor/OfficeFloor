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

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpMethod.HttpMethodEnum;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.build.HttpSecurableBuilder;
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
	 *            Name of the logic {@link Class}.
	 * @return <code>this</code>.
	 */
	WebTemplate setLogicClass(String logicClassName);

	/**
	 * <p>
	 * Specifies the {@link Method} name on the logic {@link Class} to provide the
	 * values for redirect path parameters to this {@link WebTemplate}. The returned
	 * type from the {@link Method} will be used to source values for parameters in
	 * constructing the path to this {@link WebTemplate}.
	 * <p>
	 * Should the path to the template be static (i.e. has no path parameters), no
	 * {@link Method} need be specified.
	 * 
	 * @param functionName
	 *            Logic {@link Class} {@link Method} name.
	 * @return <code>this</code>.
	 */
	WebTemplate setRedirectValuesFunction(String functionName);

	/**
	 * Specifies the <code>Content-Type</code> output by this {@link WebTemplate}.
	 * 
	 * @param contentType
	 *            <code>Content-Type</code> output by this {@link WebTemplate}.
	 * @return <code>this</code>.
	 */
	WebTemplate setContentType(String contentType);

	/**
	 * Allow overriding the default {@link Charset} to render the
	 * {@link WebTemplate}.
	 * 
	 * @param charsetName
	 *            Name of {@link Charset} to render the {@link WebTemplate}.
	 * @return <code>this</code>.
	 */
	WebTemplate setCharset(String charsetName);

	/**
	 * Allows overriding the default separator character between the path and link.
	 * 
	 * @param separator
	 *            Separator to use to separate path and link.
	 * @return <code>this</code>.
	 */
	WebTemplate setLinkSeparatorCharacter(char separator);

	/**
	 * <p>
	 * Indicate whether a secure connection is required for the link. This overrides
	 * the default template secure setting for the link.
	 * <p>
	 * Example use could be the landing page may be insecure but the login form
	 * submission link on the page is to be secure.
	 * 
	 * @param linkName
	 *            Name of link to secure.
	 * @param isSecure
	 *            <code>true</code> should the link require a secure
	 *            {@link ServerHttpConnection}.
	 * @return <code>this</code>.
	 */
	WebTemplate setLinkSecure(String linkName, boolean isSecure);

	/**
	 * <p>
	 * Obtains the {@link HttpSecurableBuilder} to configure access controls to this
	 * {@link WebTemplate}.
	 * <p>
	 * Calling this method without providing configuration requires only
	 * authentication to access the {@link WebTemplate}.
	 * 
	 * @return {@link HttpSecurableBuilder}.
	 */
	HttpSecurableBuilder getHttpSecurer();

	/**
	 * <p>
	 * Adds a {@link HttpMethod} that will render the {@link WebTemplate}. Should
	 * the {@link HttpMethod} not be in this list, a redirect will occur to
	 * {@link HttpMethodEnum#GET}.
	 * <p>
	 * Note that {@link HttpMethodEnum#GET} is added by default.
	 * 
	 * @param httpMethodName
	 *            Name of the {@link HttpMethod} that will render the
	 *            {@link WebTemplate}.
	 * @return <code>this</code>.
	 */
	WebTemplate addRenderHttpMethod(String httpMethodName);

	/**
	 * Specifies the super (parent) {@link WebTemplate}.
	 * 
	 * @param superTemplate
	 *            Super {@link WebTemplate}.
	 * @return <code>this</code>.
	 */
	WebTemplate setSuperTemplate(WebTemplate superTemplate);

	/**
	 * Adds a {@link WebTemplateExtension} for this {@link WebTemplate}.
	 * 
	 * @param extension
	 *            {@link WebTemplateExtension} {@link Class}.
	 * @return {@link WebTemplateExtensionBuilder} to build the
	 *         {@link WebTemplateExtension}.
	 */
	WebTemplateExtensionBuilder addExtension(String webTemplateExtensionClassName);

	/**
	 * Obtains the {@link OfficeFlowSinkNode} to render the {@link WebTemplate}.
	 * 
	 * @param valuesType
	 *            Name of type provided as a parameter to the
	 *            {@link OfficeFlowSinkNode} should the path parameters require
	 *            being obtained. The type should provide a bean property for each
	 *            path parameter for the {@link WebTemplate}. May be
	 *            <code>null</code> if no path parameters are required.
	 * @return {@link OfficeFlowSinkNode} to render the {@link WebTemplate}.
	 */
	OfficeFlowSinkNode getRender(String valuesTypeName);

	/**
	 * Obtains the {@link OfficeFlowSourceNode} from the {@link WebTemplate}.
	 * 
	 * @param outputName
	 *            {@link OfficeFlowSourceNode} name.
	 * @return {@link OfficeFlowSourceNode} for the name.
	 */
	OfficeFlowSourceNode getOutput(String outputName);

	/**
	 * <p>
	 * Adds {@link Governance} for this {@link WebTemplate}.
	 * <p>
	 * This enables providing {@link Governance} over all
	 * {@link OfficeSectionFunction} instances within the {@link WebTemplate} and
	 * all its subsequent {@link OfficeSubSection} instances.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 * @return <code>this</code>.
	 */
	WebTemplate addGovernance(OfficeGovernance governance);

}