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
package net.officefloor.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.session.HttpSessionManagedObjectSource;
import net.officefloor.web.session.object.HttpSessionObjectManagedObjectSource;
import net.officefloor.web.state.HttpApplicationObjectManagedObjectSource;
import net.officefloor.web.state.HttpApplicationStateManagedObjectSource;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link WebArchitect} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebArchitectEmployer implements WebArchitect {

	/**
	 * Employs a {@link WebArchitect}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 * @return {@link WebArchitect}.
	 */
	public static WebArchitect employWebArchitect(OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		return new WebArchitectEmployer(officeArchitect, officeSourceContext);
	}

	/**
	 * Prefix for the link service {@link ManagedFunction} name.
	 */
	private static final String LINK_SERVICE_FUNCTION_NAME_PREFIX = "LINK_";

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext officeSourceContext;

	/**
	 * Registry of HTTP Application Object to its {@link OfficeManagedObject}.
	 */
	private final Map<String, OfficeManagedObject> httpApplicationObjects = new HashMap<>();

	/**
	 * Registry of HTTP Session Object to its {@link OfficeManagedObject}.
	 */
	private final Map<String, OfficeManagedObject> httpSessionObjects = new HashMap<>();

	/**
	 * Registry of HTTP Request Object to its {@link OfficeManagedObject}.
	 */
	private final Map<String, OfficeManagedObject> httpRequestObjects = new HashMap<>();

	/**
	 * {@link ChainedServicer} instances.
	 */
	private final List<ChainedServicer> chainedServicers = new LinkedList<ChainedServicer>();

	/**
	 * Instantiate.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 */
	private WebArchitectEmployer(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;
	}

	/**
	 * Obtains the bind name for the {@link OfficeManagedObject}.
	 * 
	 * @param objectClass
	 *            {@link Class} of the {@link Object}.
	 * @param bindName
	 *            Optional bind name. May be <code>null</code>.
	 * @return Bind name for the {@link OfficeManagedObject};
	 */
	private String getBindName(Class<?> objectClass, String bindName) {
		return (bindName == null ? objectClass.getName() : bindName);
	}

	/*
	 * ======================== WebArchitect =========================
	 */

	@Override
	public OfficeManagedObject addHttpApplicationObject(Class<?> objectClass, String bindName) {

		// Determine if already registered
		bindName = getBindName(objectClass, bindName);
		OfficeManagedObject object = this.httpApplicationObjects.get(bindName);
		if (object != null) {
			return object; // return the already registered object
		}

		// Not registered, so register
		OfficeManagedObjectSource mos = this.officeArchitect.addOfficeManagedObjectSource(bindName,
				HttpApplicationObjectManagedObjectSource.class.getName());
		mos.addProperty(HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME, objectClass.getName());
		if ((bindName != null) && (bindName.trim().length() > 0)) {
			mos.addProperty(HttpApplicationObjectManagedObjectSource.PROPERTY_BIND_NAME, bindName);
		}
		object = mos.addOfficeManagedObject(bindName, ManagedObjectScope.PROCESS);
		this.httpApplicationObjects.put(bindName, object);

		// Return the object
		return object;
	}

	@Override
	public OfficeManagedObject addHttpApplicationObject(Class<?> objectClass) {
		return this.addHttpApplicationObject(objectClass, null);
	}

	@Override
	public OfficeManagedObject addHttpSessionObject(Class<?> objectClass, String bindName) {

		// Determine if already registered
		bindName = getBindName(objectClass, bindName);
		OfficeManagedObject object = this.httpSessionObjects.get(objectClass);
		if (object != null) {
			return object; // return the already registered object
		}

		// Not registered, so register
		OfficeManagedObjectSource mos = this.officeArchitect.addOfficeManagedObjectSource(bindName,
				HttpSessionObjectManagedObjectSource.class.getName());
		mos.addProperty(HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME, objectClass.getName());
		if ((bindName != null) && (bindName.trim().length() > 0)) {
			mos.addProperty(HttpSessionObjectManagedObjectSource.PROPERTY_BIND_NAME, bindName);
		}
		object = mos.addOfficeManagedObject(bindName, ManagedObjectScope.PROCESS);
		this.httpSessionObjects.put(bindName, object);

		// Return the object
		return object;
	}

	@Override
	public OfficeManagedObject addHttpSessionObject(Class<?> objectClass) {
		return this.addHttpSessionObject(objectClass, null);
	}

	@Override
	public OfficeManagedObject addHttpRequestObject(Class<?> objectClass, boolean isLoadParameters, String bindName) {

		// Determine if already registered
		bindName = getBindName(objectClass, bindName);
		OfficeManagedObject object = this.httpRequestObjects.get(bindName);
		if (object == null) {

			// Not registered, so register
			OfficeManagedObjectSource mos = this.officeArchitect.addOfficeManagedObjectSource(bindName,
					HttpRequestObjectManagedObjectSource.class.getName());
			mos.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME, objectClass.getName());
			if ((bindName != null) && (bindName.trim().length() > 0)) {
				mos.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_BIND_NAME, bindName);
			}
			object = mos.addOfficeManagedObject(bindName, ManagedObjectScope.PROCESS);
			this.httpRequestObjects.put(bindName, object);

			// Determine if load HTTP parameters
			if (isLoadParameters) {

				// Add the property to load parameters
				mos.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
						String.valueOf(true));
			}
		}

		// Return the object
		return object;
	}

	@Override
	public OfficeManagedObject addHttpRequestObject(Class<?> objectClass, boolean isLoadParameters) {
		return this.addHttpRequestObject(objectClass, isLoadParameters, null);
	}

//	@Override
//	public HttpUrlContinuation linkUri(String httpMethod, String uri, OfficeSectionInput sectionInput) {
//		return this.urlContinuations.linkUri(httpMethod, uri, sectionInput);
//	}

	@Override
	public void chainServicer(OfficeSectionInput sectionInput, OfficeSectionOutput notHandledOutput) {
		this.chainedServicers.add(new ChainedServicer(sectionInput, notHandledOutput));
	}

//	@Override
//	public String[] getURIs() {
//
//		// Create the URIs
//		List<String> uris = new LinkedList<String>();
//
//		// Add the linked URIs
//		for (String uri : this.urlContinuations.getRegisteredHttpUris()) {
//
//			// Obtain the URI path
//			try {
//				uri = HttpUrlContinuationManagedFunctionSource.getApplicationUriPath(uri);
//			} catch (InvalidRequestUriHttpException ex) {
//				// Do nothing and keep URI path as is
//			}
//
//			// Add the URI path
//			uris.add(uri);
//		}
//
//		// Return the URIs
//		return uris.toArray(new String[uris.size()]);
//	}

	@Override
	public void informOfficeArchitect() {

		// Auto wire the objects
		this.officeArchitect.enableAutoWireObjects();

		// Configure HTTP Session (allowing 10 seconds to retrieve session)
		OfficeManagedObjectSource httpSessionMos = this.officeArchitect.addOfficeManagedObjectSource("HTTP_SESSION",
				HttpSessionManagedObjectSource.class.getName());
		httpSessionMos.setTimeout(10 * 1000); // TODO make configurable
		httpSessionMos.addOfficeManagedObject("HTTP_SESSION", ManagedObjectScope.PROCESS);

		// Configure the HTTP Application and Request States
		this.officeArchitect
				.addOfficeManagedObjectSource("HTTP_APPLICATION_STATE",
						HttpApplicationStateManagedObjectSource.class.getName())
				.addOfficeManagedObject("HTTP_APPLICATION_STATE", ManagedObjectScope.PROCESS);
		this.officeArchitect
				.addOfficeManagedObjectSource("HTTP_REQUEST_STATE", HttpRequestStateManagedObjectSource.class.getName())
				.addOfficeManagedObject("HTTP_REQUEST_STATE", ManagedObjectScope.PROCESS);

//		// Add the HTTP section
//		OfficeSection httpSection = this.officeArchitect.addOfficeSection(HANDLER_SECTION_NAME,
//				WebApplicationSectionSource.class.getName(), null);
//		httpSection.addProperty(WebApplicationSectionSource.PROPERTY_LINK_SERVICE_FUNCTION_NAME_PREFIX,
//				LINK_SERVICE_FUNCTION_NAME_PREFIX);
//
//		// Add location
//		OfficeManagedObjectSource locationMos = this.officeArchitect.addOfficeManagedObjectSource(
//				HttpApplicationLocation.class.getName(), HttpApplicationLocationManagedObjectSource.class.getName());
//		HttpApplicationLocationManagedObjectSource.copyProperties(officeSourceContext, locationMos);
//		locationMos.addOfficeManagedObject(HttpApplicationLocation.class.getName(), ManagedObjectScope.PROCESS);
//
//		// Chain the servicers
//		OfficeSectionOutput previousChainedOutput = httpSection
//				.getOfficeSectionOutput(WebApplicationSectionSource.UNHANDLED_REQUEST_OUTPUT_NAME);
//		for (ChainedServicer chainedServicer : this.chainedServicers) {
//
//			// Link the chained servicer (if previous chained output)
//			if (previousChainedOutput != null) {
//				this.officeArchitect.link(previousChainedOutput, chainedServicer.sectionInput);
//			}
//
//			// Configure for next in chain
//			previousChainedOutput = chainedServicer.notHandledOutput;
//		}
//
//		// End chain with file sender servicer (if previous chained output)
//		if (previousChainedOutput != null) {
//
//			// TODO configure chained servicers
//
//			// TODO configure 404 not found
//		}
	}

	/**
	 * Chained servicer.
	 */
	private static class ChainedServicer {

		/**
		 * {@link OfficeSectionInput}.
		 */
		public final OfficeSectionInput sectionInput;

		/**
		 * Name of the {@link SectionOutput}. May be <code>null</code>.
		 */
		public final OfficeSectionOutput notHandledOutput;

		/**
		 * Initiate.
		 * 
		 * @param sectionInput
		 *            {@link OfficeSectionInput}.
		 * @param notHandledOutput
		 *            {@link SectionOutput}. May be <code>null</code>.
		 */
		public ChainedServicer(OfficeSectionInput sectionInput, OfficeSectionOutput notHandledOutput) {
			this.sectionInput = sectionInput;
			this.notHandledOutput = notHandledOutput;
		}
	}

	@Override
	public void setContextPath(String contextPath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HttpUrlContinuation link(boolean isSecure, String applicationPath, OfficeSectionInput sectionInput) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpInput link(boolean isSecure, HttpMethod httpMethod, String applicationPath,
			OfficeSectionInput sectionInput) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void link(OfficeSectionOutput output, HttpUrlContinuation continuation, Class<?> parameterType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HttpInput[] getHttpInputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void intercept(OfficeSectionInput sectionInput, OfficeSectionOutput sectionOutput) {
		// TODO Auto-generated method stub
		
	}

}