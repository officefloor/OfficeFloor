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
package net.officefloor.web.build;

import java.lang.annotation.Annotation;
import java.util.ServiceLoader;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.HttpObject;
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
	 * Name of the {@link OfficeSectionInput} that handles the
	 * {@link HttpRequest} instances.
	 */
	static String HANDLER_INPUT_NAME = "HANDLE_HTTP_INPUT";

	/**
	 * <p>
	 * Manually adds a {@link HttpObjectParserFactory}.
	 * <p>
	 * Typically these should be configured via {@link ServiceLoader}, so can be
	 * plugged in as required.
	 * 
	 * @param objectParserFactory
	 *            {@link HttpObjectParserFactory}.
	 */
	void addHttpObjectParser(HttpObjectParserFactory objectParserFactory);

	/**
	 * <p>
	 * Adds another {@link Class} as an alias for the {@link HttpObject}
	 * annotation.
	 * <p>
	 * As code generators are likely to be used for the HTTP objects, it is not
	 * always possible to generate the {@link Class} annotated with
	 * {@link HttpObject}. This allows another {@link Annotation} to indicate
	 * the parameter object is a HTTP object.
	 * 
	 * @param httpObjectAnnotationAliasClass
	 *            Alias {@link Annotation} {@link Class} for {@link HttpObject}.
	 * @param acceptedContentTypes
	 *            Listing of the <code>content-type</code> values accepted. May
	 *            be empty array to allow supporting all available
	 *            <code>content-type</code> {@link HttpObjectParser} instances
	 *            available.
	 */
	void addHttpObjectAnnotationAlias(Class<?> httpObjectAnnotationAliasClass, String... acceptedContentTypes);

	/**
	 * Adds a {@link HttpObjectResponderFactory}.
	 * 
	 * @param objectResponderFactory
	 *            {@link HttpObjectResponderFactory}.
	 */
	void addHttpObjectResponder(HttpObjectResponderFactory objectResponderFactory);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpApplicationState}.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param bindName
	 *            Name to bind the object within the
	 *            {@link HttpApplicationState}.
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
	 * @param objectClass
	 *            Class of the object.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpApplicationObject(Class<?> objectClass);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpSession}.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param bindName
	 *            Name to bind the object within the {@link HttpSession}.
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
	 * @param objectClass
	 *            Class of the object.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpSessionObject(Class<?> objectClass);

	/**
	 * Adds an object to be lazily created and stored within the
	 * {@link HttpRequestState}.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param isLoadParameters
	 *            Indicates whether to load the HTTP parameters to instantiated
	 *            objects.
	 * @param bindName
	 *            Name to bind the object within the {@link HttpRequestState}.
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
	 * @param objectClass
	 *            Class of the object.
	 * @param isLoadParameters
	 *            Indicates whether to load the HTTP parameters to instantiated
	 *            objects.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpRequestObject(Class<?> objectClass, boolean isLoadParameters);

	/**
	 * Adds a HTTP argument.
	 * 
	 * @param parameterName
	 *            Name of the parameter.
	 * @param location
	 *            {@link HttpValueLocation} to obtain the argument value. May be
	 *            <code>null</code> to obtain from anywhere on the
	 *            {@link HttpRequest}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpArgument(String parameterName, HttpValueLocation location);

	/**
	 * Adds a HTTP {@link Object} to be parsed from the {@link HttpRequest}.
	 * 
	 * @param objectClass
	 *            Class of the object.
	 * @param acceptedContentTypes
	 *            Listing of the <code>content-type</code> values accepted. May
	 *            be empty array to allow supporting all available
	 *            <code>content-type</code> {@link HttpObjectParser} instances
	 *            available.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addHttpObject(Class<?> objectClass, String... acceptedContentTypes);

	/**
	 * Indicates if the path contains path parameters.
	 * 
	 * @param path
	 *            Path.
	 * @return <code>true</code> if path contains parameters.
	 */
	boolean isPathParameters(String path);

	/**
	 * Links a URL to an {@link OfficeSectionInput}. This will always be a
	 * {@link HttpMethod#GET} due to redirection required for the
	 * {@link HttpUrlContinuation}.
	 * 
	 * @param isSecure
	 *            Indicates if secure connection required.
	 * @param applicationPath
	 *            URL path of the application to be linked.
	 * @param sectionInput
	 *            {@link OfficeSectionInput} servicing the URI.
	 * @return {@link HttpUrlContinuation}.
	 */
	HttpUrlContinuation link(boolean isSecure, String applicationPath, OfficeSectionInput sectionInput);

	/**
	 * Links a URL to an {@link OfficeSectionInput}.
	 * 
	 * @param isSecure
	 *            Indicates if secure connection required.
	 * @param httpMethod
	 *            {@link HttpMethod}.
	 * @param applicationPath
	 *            URL path of the application to be linked.
	 * @param sectionInput
	 *            {@link OfficeSectionInput} servicing the URI.
	 * @return {@link HttpInput}.
	 */
	HttpInput link(boolean isSecure, HttpMethod httpMethod, String applicationPath, OfficeSectionInput sectionInput);

	/**
	 * Links the {@link OfficeSectionOutput} to the {@link HttpUrlContinuation}.
	 * 
	 * @param output
	 *            {@link OfficeSectionOutput}.
	 * @param continuation
	 *            {@link HttpUrlContinuation}.
	 * @param parameterType
	 *            {@link Class} providing the possible parameters for the
	 *            {@link HttpUrlContinuation} path. May be <code>null</code> if
	 *            {@link HttpUrlContinuation} path contains no parameters.
	 */
	void link(OfficeSectionOutput output, HttpUrlContinuation continuation, Class<?> parameterType);

	/**
	 * <p>
	 * Intercepts all {@link HttpRequest} instances before servicing. Multiple
	 * intercepts may be configured, with them executed in the order they are
	 * added.
	 * <p>
	 * This allows, for example, logging all requests to the web application.
	 * 
	 * @param sectionInput
	 *            {@link OfficeSectionInput} to handle intercepting the
	 *            {@link HttpRequest}.
	 * @param sectionOutput
	 *            {@link OfficeSectionOutput} to continue servicing the
	 *            {@link HttpRequest}.
	 */
	void intercept(OfficeSectionInput sectionInput, OfficeSectionOutput sectionOutput);

	/**
	 * <p>
	 * Chains a {@link OfficeSectionInput} to the end of the servicing chain to
	 * handle a {@link HttpRequest}. Multiple chained services may be added,
	 * with them executed in the order they are added.
	 * <p>
	 * The {@link WebArchitect} functionality is always the first in the chain
	 * to attempt to service the {@link HttpRequest}. This allows, for example,
	 * adding a chained servicer for serving resources from a file system.
	 * 
	 * @param sectionInput
	 *            {@link OfficeSectionInput} to handle the {@link HttpRequest}.
	 * @param notHandledOutput
	 *            {@link OfficeSectionOutput} should this servicer not handle
	 *            the {@link HttpRequest}. May be <code>null</code> if handles
	 *            all {@link HttpRequest} instances (any services chained after
	 *            this will therefore not be used).
	 */
	void chainServicer(OfficeSectionInput sectionInput, OfficeSectionOutput notHandledOutput);

	/**
	 * Informs the {@link OfficeArchitect} of the web architect. This is to be
	 * invoked once all web architecture is configured.
	 */
	void informOfficeArchitect();

}