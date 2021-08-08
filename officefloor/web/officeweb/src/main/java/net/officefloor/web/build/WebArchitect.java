/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import java.lang.annotation.Annotation;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpObject;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpApplicationState;
import net.officefloor.web.state.HttpRequestState;

/**
 * Web configuration extensions for the {@link OfficeArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract interface WebArchitect {

	/**
	 * Name of the {@link OfficeSection} that handles the {@link HttpRequest}
	 * instances.
	 */
	static String HANDLER_SECTION_NAME = "HANDLE_HTTP_SECTION";

	/**
	 * Name of the {@link OfficeSectionInput} that handles the {@link HttpRequest}
	 * instances.
	 */
	static String HANDLER_INPUT_NAME = "HANDLE_HTTP_INPUT";

	/**
	 * <p>
	 * Manually adds a {@link HttpObjectParserFactory}.
	 * <p>
	 * Typically these should be configured via {@link HttpObjectParserServiceFactory}, so can be
	 * plugged in as required.
	 * 
	 * @param objectParserFactory {@link HttpObjectParserFactory}.
	 */
	void addHttpObjectParser(HttpObjectParserFactory objectParserFactory);

	/**
	 * Specifies the default {@link HttpObjectParserServiceFactory}.
	 * 
	 * @param objectParserServiceFactory Default
	 *                                   {@link HttpObjectParserServiceFactory}.
	 */
	void setDefaultHttpObjectParser(HttpObjectParserServiceFactory objectParserServiceFactory);

	/**
	 * <p>
	 * Adds another {@link Class} as an alias for the {@link HttpObject} annotation.
	 * <p>
	 * As code generators are likely to be used for the HTTP objects, it is not
	 * always possible to generate the {@link Class} annotated with
	 * {@link HttpObject}. This allows another {@link Annotation} to indicate the
	 * parameter object is a HTTP object.
	 * 
	 * @param httpObjectAnnotationAliasClass Alias {@link Annotation} {@link Class}
	 *                                       for {@link HttpObject}.
	 * @param acceptedContentTypes           Listing of the
	 *                                       <code>content-type</code> values
	 *                                       accepted. May be empty array to allow
	 *                                       supporting all available
	 *                                       <code>content-type</code>
	 *                                       {@link HttpObjectParser} instances
	 *                                       available.
	 */
	void addHttpObjectAnnotationAlias(Class<?> httpObjectAnnotationAliasClass, String... acceptedContentTypes);

	/**
	 * Adds a {@link HttpObjectResponderFactory}.
	 * 
	 * @param objectResponderFactory {@link HttpObjectResponderFactory}.
	 */
	void addHttpObjectResponder(HttpObjectResponderFactory objectResponderFactory);

	/**
	 * Specifies the default {@link HttpObjectResponderServiceFactory}.
	 * 
	 * @param objectResponderServiceFactory Default
	 *                                      {@link HttpObjectResponderServiceFactory}.
	 */
	void setDefaultHttpObjectResponder(HttpObjectResponderServiceFactory objectResponderServiceFactory);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpApplicationState}.
	 * 
	 * @param objectClass Class of the object.
	 * @param bindName    Name to bind the object within the
	 *                    {@link HttpApplicationState}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpApplicationObject(Class<?> objectClass, String bindName);

	/**
	 * <p>
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpApplicationState}.
	 * <p>
	 * The bound name is arbitrarily chosen but will be unique for the object.
	 * 
	 * @param objectClass Class of the object.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpApplicationObject(Class<?> objectClass);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpSession}.
	 * 
	 * @param objectClass Class of the object.
	 * @param bindName    Name to bind the object within the {@link HttpSession}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpSessionObject(Class<?> objectClass, String bindName);

	/**
	 * <p>
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpSession}.
	 * <p>
	 * The bound name is arbitrarily chosen but will be unique for the object.
	 * 
	 * @param objectClass Class of the object.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpSessionObject(Class<?> objectClass);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpRequestState}.
	 * 
	 * @param objectClass      Class of the object.
	 * @param isLoadParameters Indicates whether to load the HTTP parameters to
	 *                         instantiated objects.
	 * @param bindName         Name to bind the object within the
	 *                         {@link HttpRequestState}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpRequestObject(Class<?> objectClass, boolean isLoadParameters, String bindName);

	/**
	 * <p>
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpRequestState}.
	 * <p>
	 * The bound name is arbitrarily chosen but will be unique for the object.
	 * 
	 * @param objectClass      Class of the object.
	 * @param isLoadParameters Indicates whether to load the HTTP parameters to
	 *                         instantiated objects.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpRequestObject(Class<?> objectClass, boolean isLoadParameters);

	/**
	 * Adds a HTTP argument.
	 * 
	 * @param parameterName Name of the parameter.
	 * @param location      {@link HttpValueLocation} to obtain the argument value.
	 *                      May be <code>null</code> to obtain from anywhere on the
	 *                      {@link HttpRequest}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpArgument(String parameterName, HttpValueLocation location);

	/**
	 * Adds a HTTP {@link Object} to be parsed from the {@link HttpRequest}.
	 * 
	 * @param objectClass          Class of the object.
	 * @param acceptedContentTypes Listing of the <code>content-type</code> values
	 *                             accepted. May be empty array to allow supporting
	 *                             all available <code>content-type</code>
	 *                             {@link HttpObjectParser} instances available.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpObject(Class<?> objectClass, String... acceptedContentTypes);

	/**
	 * Indicates if the path contains path parameters.
	 * 
	 * @param path Path.
	 * @return <code>true</code> if path contains parameters.
	 */
	boolean isPathParameters(String path);

	/**
	 * Creates a {@link HttpUrlContinuation} into the web application. This will
	 * always be a {@link HttpMethod#GET} due to redirection required for the
	 * {@link HttpUrlContinuation}.
	 * 
	 * @param isSecure        Indicates if secure connection required.
	 * @param applicationPath Application path to be linked.
	 * @return {@link HttpUrlContinuation}.
	 */
	HttpUrlContinuation getHttpInput(boolean isSecure, String applicationPath);

	/**
	 * Creates a {@link HttpInput} into the application.
	 * 
	 * @param isSecure        Indicates if secure connection required.
	 * @param httpMethodName  Name of the {@link HttpMethod}.
	 * @param applicationPath URL path of the application to be linked.
	 * @return {@link HttpInput}.
	 */
	HttpInput getHttpInput(boolean isSecure, String httpMethodName, String applicationPath);

	/**
	 * Adds a {@link HttpInputExplorer}.
	 * 
	 * @param explorer {@link HttpInputExplorer}.
	 */
	void addHttpInputExplorer(HttpInputExplorer explorer);

	/**
	 * <p>
	 * Runs the {@link ServerHttpConnection} through routing again.
	 * <p>
	 * Typically, this is used on importing previous state into the
	 * {@link ServerHttpConnection} and then have it serviced.
	 * 
	 * @param flowSourceNode {@link OfficeFlowSourceNode} to trigger re-routing the
	 *                       {@link ServerHttpConnection}.
	 */
	void reroute(OfficeFlowSourceNode flowSourceNode);

	/**
	 * <p>
	 * Intercepts all {@link HttpRequest} instances before servicing. Multiple
	 * intercepts may be configured, with them executed in the order they are added.
	 * <p>
	 * This allows, for example, logging all requests to the web application.
	 * 
	 * @param flowSinkNode   {@link OfficeFlowSinkNode} to handle intercepting the
	 *                       {@link HttpRequest}.
	 * @param flowSourceNode {@link OfficeFlowSourceNode} to continue servicing the
	 *                       {@link HttpRequest}.
	 */
	void intercept(OfficeFlowSinkNode flowSinkNode, OfficeFlowSourceNode flowSourceNode);

	/**
	 * <p>
	 * Chains a {@link OfficeSectionInput} to the end of the servicing chain to
	 * handle a {@link HttpRequest}. Multiple chained services may be added, with
	 * them executed in the order they are added.
	 * <p>
	 * The {@link WebArchitect} functionality is always the first in the chain to
	 * attempt to service the {@link HttpRequest}. This allows, for example, adding
	 * a chained servicer for serving resources from a file system.
	 * 
	 * @param flowSinkNode     {@link OfficeFlowSinkNode} to handle the
	 *                         {@link HttpRequest}.
	 * @param notHandledOutput {@link OfficeFlowSourceNode} should this servicer not
	 *                         handle the {@link HttpRequest}. May be
	 *                         <code>null</code> if handles all {@link HttpRequest}
	 *                         instances (any services chained after this will
	 *                         therefore not be used).
	 */
	void chainServicer(OfficeFlowSinkNode flowSinkNode, OfficeFlowSourceNode notHandledOutput);

	/**
	 * Creates the {@link AcceptNegotiatorBuilder} to build an
	 * {@link AcceptNegotiator}.
	 * 
	 * @param <H> Handler type.
	 * @return {@link AcceptNegotiatorBuilder} to build an {@link AcceptNegotiator}.
	 */
	<H> AcceptNegotiatorBuilder<H> createAcceptNegotiator();

	/**
	 * Informs the {@link OfficeArchitect} of the web architect. This is to be
	 * invoked once all web architecture is configured.
	 */
	void informOfficeArchitect();

}
