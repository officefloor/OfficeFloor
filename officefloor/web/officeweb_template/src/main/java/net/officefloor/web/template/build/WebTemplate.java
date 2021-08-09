/*-
 * #%L
 * Web Template
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	 * @param logicClassName
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
	 * @param webTemplateExtensionClassName
	 *            {@link WebTemplateExtension} {@link Class} name.
	 * @return {@link WebTemplateExtensionBuilder} to build the
	 *         {@link WebTemplateExtension}.
	 */
	WebTemplateExtensionBuilder addExtension(String webTemplateExtensionClassName);

	/**
	 * Obtains the {@link OfficeFlowSinkNode} to render the {@link WebTemplate}.
	 * 
	 * @param valuesTypeName
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
