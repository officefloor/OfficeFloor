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

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationManagedFunctionSource;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationSectionSource;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.web.http.security.AnonymousHttpAuthenticationManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpAuthenticationRequiredException;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoader;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoaderImpl;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.template.HttpTemplateManagedFunctionSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialManagedFunctionSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;
import net.officefloor.web.HttpApplicationStateful;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpRequestStateful;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.WebApplicationSectionSource;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.path.HttpApplicationLocation;
import net.officefloor.web.path.HttpApplicationLocationManagedObjectSource;
import net.officefloor.web.resource.source.HttpFileSectionSource;
import net.officefloor.web.route.InvalidRequestUriHttpException;
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
public class WebApplicationArchitectEmployer implements WebArchitect {

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
		return new WebApplicationArchitectEmployer(officeArchitect, officeSourceContext);
	}

	/**
	 * Obtains the {@link HttpTemplate} section name from the
	 * {@link HttpTemplate} URI.
	 * 
	 * @param templateUri
	 *            {@link HttpTemplate} URI.
	 * @return {@link HttpTemplate} section name.
	 */
	public static String getTemplateSectionName(String templateUri) {

		// Determine section name
		String sectionName;
		if ("/".equals(templateUri)) {
			// Root template
			sectionName = "_root_";

		} else {
			// Use template URI stripping off leading '/'
			sectionName = (templateUri.startsWith("/") ? templateUri.substring("/".length()) : templateUri);
		}

		// Return the section name
		return sectionName;
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
	 * {@link HttpTemplateSection} instances.
	 */
	private final List<HttpTemplateSectionImpl> httpTemplates = new LinkedList<>();

	/**
	 * Default {@link HttpTemplate} URI suffix.
	 */
	private String defaultTemplateUriSuffix = null;

	/**
	 * {@link HttpUrlContinuationSectionSource} to provide configured URL
	 * continuations.
	 */
	private final HttpUrlContinuationSectionSource urlContinuations = new HttpUrlContinuationSectionSource();

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
	 * {@link HttpSecurity} instances.
	 */
	private final List<HttpSecuritySectionImpl<?, ?, ?, ?>> httpSecurities = new LinkedList<>();

	/**
	 * {@link ResourceLink} instances.
	 */
	private final List<ResourceLink> resourceLinks = new LinkedList<ResourceLink>();

	/**
	 * {@link EscalationResource} instances.
	 */
	private final List<EscalationResource> escalationResources = new LinkedList<EscalationResource>();

	/**
	 * {@link SendLink} instances.
	 */
	private final List<SendLink> sendLinks = new LinkedList<SendLink>();

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
	private WebApplicationArchitectEmployer(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;

		// Configure the section transformer for URL continuations
		this.officeArchitect.addOfficeSectionTransformer(this.urlContinuations);
	}

	/**
	 * Obtains the {@link HttpTemplate} URI suffix.
	 * 
	 * @param template
	 *            {@link HttpTemplateSectionImpl}.
	 * @return {@link HttpTemplate} URI suffix.
	 */
	private String getTemplateUriSuffix(HttpTemplateSectionImpl template) {

		// Obtain the template URI Suffix
		String templateUriSuffix = template.getTemplateUriSuffix();
		if (templateUriSuffix == null) {
			templateUriSuffix = (this.defaultTemplateUriSuffix == null ? "" : this.defaultTemplateUriSuffix);
		}

		// Return the URI suffix
		return templateUriSuffix;
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
	public HttpTemplateSection addHttpTemplate(String templateUri, String templateLocation,
			final Class<?> templateLogicClass) {

		// Obtain canonical template URI path
		templateUri = HttpUrlContinuationManagedFunctionSource.getApplicationUriPath(templateUri);

		// Ensure URI is not already registered
		for (HttpTemplateSectionImpl template : this.httpTemplates) {
			if (templateUri.equals(template.getTemplateUri())) {
				throw new IllegalStateException("HTTP Template already added for URI '" + templateUri + "'");
			}
		}

		// Determine section name from template URI
		String sectionName = getTemplateSectionName(templateUri);

		// Add the HTTP template section
		OfficeSection section = this.officeArchitect.addOfficeSection(sectionName,
				HttpTemplateSectionSource.class.getName(), templateLocation);
		if (templateLogicClass != null) {
			section.addProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME, templateLogicClass.getName());
		}

		// Create and register the HTTP template
		HttpTemplateSectionImpl template = new HttpTemplateSectionImpl(section, templateLogicClass, templateLocation,
				templateUri);
		this.httpTemplates.add(template);

		// Add the annotated parameters (if have template logic class)
		if (templateLogicClass != null) {
			for (Method method : templateLogicClass.getMethods()) {
				for (Class<?> parameterType : method.getParameterTypes()) {

					// HTTP Application Stateful
					HttpApplicationStateful applicationAnnotation = parameterType
							.getAnnotation(HttpApplicationStateful.class);
					if (applicationAnnotation != null) {
						this.addHttpApplicationObject(parameterType, applicationAnnotation.value());
					}

					// HTTP Session Stateful
					HttpSessionStateful sessionAnnotation = parameterType.getAnnotation(HttpSessionStateful.class);
					if (sessionAnnotation != null) {
						this.addHttpSessionObject(parameterType, sessionAnnotation.value());
					}

					// HTTP Request Stateful
					HttpRequestStateful requestAnnotation = parameterType.getAnnotation(HttpRequestStateful.class);
					if (requestAnnotation != null) {
						this.addHttpRequestObject(parameterType, false, requestAnnotation.value());
					}

					// HTTP Parameters
					HttpParameters parameters = parameterType.getAnnotation(HttpParameters.class);
					if (parameters != null) {
						this.addHttpRequestObject(parameterType, true, parameters.value());
					}
				}
			}
		}

		// Return the template
		return template;
	}

	@Override
	public void setDefaultHttpTemplateUriSuffix(String uriSuffix) {
		this.defaultTemplateUriSuffix = uriSuffix;
	}

	@Override
	public HttpSecurity addHttpSecurity(String securityName,
			final Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass) {

		// Instantiate the HTTP security source
		HttpSecuritySource<?, ?, ?, ?> httpSecuritySource;
		try {
			httpSecuritySource = (HttpSecuritySource<?, ?, ?, ?>) this.officeSourceContext
					.loadClass(httpSecuritySourceClass.getName()).newInstance();
		} catch (IllegalAccessException | InstantiationException ex) {
			// Must be able to instantiate instance
			throw new LoadTypeError(HttpSecuritySource.class, httpSecuritySourceClass.getName(), null);
		}

		// Create and register the HTTP security
		HttpSecuritySectionImpl<?, ?, ?, ?> httpSecurity = new HttpSecuritySectionImpl<>(this.officeArchitect,
				securityName, httpSecuritySource, this.officeSourceContext.createPropertyList());
		this.httpSecurities.add(httpSecurity);

		// Return the HTTP security
		return httpSecurity;
	}

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

	@Override
	public HttpUrlContinuation linkUri(String uri, OfficeSectionInput sectionInput) {
		return this.urlContinuations.linkUri(uri, sectionInput);
	}

	@Override
	public HttpUrlContinuation linkUri(String uri, HttpTemplateSection template) {
		return this.linkUri(uri, template.getOfficeSection()
				.getOfficeSectionInput(HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME));
	}

	@Override
	public void linkToHttpTemplate(OfficeSectionOutput sectionOutput, HttpTemplateSection template) {
		this.officeArchitect.link(sectionOutput, template.getOfficeSection()
				.getOfficeSectionInput(HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME));
	}

	@Override
	public void linkToResource(OfficeSectionOutput sectionOutput, String resourcePath) {
		this.resourceLinks.add(new ResourceLink(sectionOutput, resourcePath));
	}

	@Override
	public void linkEscalation(Class<? extends Throwable> escalation, HttpTemplateSection template) {
		OfficeEscalation officeEscalation = this.officeArchitect.addOfficeEscalation(escalation.getName());
		this.officeArchitect.link(officeEscalation, template.getOfficeSection()
				.getOfficeSectionInput(HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME));
	}

	@Override
	public void linkEscalation(Class<? extends Throwable> escalation, String resourcePath) {
		this.escalationResources.add(new EscalationResource(escalation, resourcePath));
	}

	@Override
	public void linkToSendResponse(OfficeSectionOutput sectionOutput) {
		this.sendLinks.add(new SendLink(sectionOutput));
	}

	@Override
	public void chainServicer(OfficeSectionInput sectionInput, OfficeSectionOutput notHandledOutput) {
		this.chainedServicers.add(new ChainedServicer(sectionInput, notHandledOutput));
	}

	@Override
	public String[] getURIs() {

		// Create the URIs
		List<String> uris = new LinkedList<String>();

		// Determine if root template to include
		for (HttpTemplateSectionImpl template : this.httpTemplates) {
			String templateUri = template.getTemplateUri();
			if ("/".equals(templateUri)) {
				uris.add(templateUri);
			}
		}

		// Add the linked URIs
		for (String uri : this.urlContinuations.getRegisteredHttpUris()) {

			// Obtain the URI path
			try {
				uri = HttpUrlContinuationManagedFunctionSource.getApplicationUriPath(uri);
			} catch (InvalidRequestUriHttpException ex) {
				// Do nothing and keep URI path as is
			}

			// Add the URI path
			uris.add(uri);
		}

		// Return the URIs
		return uris.toArray(new String[uris.size()]);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
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

		// Add the HTTP section
		OfficeSection httpSection = this.officeArchitect.addOfficeSection(HANDLER_SECTION_NAME,
				WebApplicationSectionSource.class.getName(), null);
		httpSection.addProperty(WebApplicationSectionSource.PROPERTY_LINK_SERVICE_FUNCTION_NAME_PREFIX,
				LINK_SERVICE_FUNCTION_NAME_PREFIX);

		// Add location
		OfficeManagedObjectSource locationMos = this.officeArchitect.addOfficeManagedObjectSource(
				HttpApplicationLocation.class.getName(), HttpApplicationLocationManagedObjectSource.class.getName());
		HttpApplicationLocationManagedObjectSource.copyProperties(officeSourceContext, locationMos);
		locationMos.addOfficeManagedObject(HttpApplicationLocation.class.getName(), ManagedObjectScope.PROCESS);

		// Load the HTTP security
		if (this.httpSecurities.size() == 0) {
			// Provide anonymous authentication
			OfficeManagedObjectSource mos = this.officeArchitect.addOfficeManagedObjectSource(
					AnonymousHttpAuthenticationManagedObjectSource.class.getName(),
					AnonymousHttpAuthenticationManagedObjectSource.class.getName());
			mos.addOfficeManagedObject(AnonymousHttpAuthenticationManagedObjectSource.class.getName(),
					ManagedObjectScope.PROCESS);

		} else {
			// Configure the securities
			OfficeEscalation authenticationRequiredEscalation = null;
			for (HttpSecuritySectionImpl<?, ?, ?, ?> httpSecurity : this.httpSecurities) {

				// Configure the security
				String securityName = httpSecurity.getOfficeSection().getOfficeSectionName();
				long securityTimeout = httpSecurity.getSecurityTimeout();

				// Load the type (which also initialises the source)
				HttpSecurityLoader securityLoader = new HttpSecurityLoaderImpl(this.officeSourceContext, securityName);
				HttpSecurityType<?, ?, ?, ?> httpSecurityType = securityLoader
						.loadHttpSecurityType(httpSecurity.getHttpSecuritySource(), httpSecurity.getProperties());

				// Load the security type
				httpSecurity.setHttpSecurityType((HttpSecurityType) httpSecurityType);

				// Obtain the security class
				Class<?> securityClass = httpSecurityType.getSecurityClass();

				// Provide flow linking
				if (authenticationRequiredEscalation == null) {
					authenticationRequiredEscalation = this.officeArchitect
							.addOfficeEscalation(HttpAuthenticationRequiredException.class.getName());
				}
				this.officeArchitect.link(authenticationRequiredEscalation,
						httpSecurity.getOfficeSection().getOfficeSectionInput("Challenge"));
				this.officeArchitect.link(httpSecurity.getOfficeSection().getOfficeSectionOutput("Recontinue"),
						httpSection.getOfficeSectionInput(HANDLER_INPUT_NAME));

				// Add the HTTP Authentication Managed Object
				OfficeManagedObjectSource httpAuthenticationMos = this.officeArchitect.addOfficeManagedObjectSource(
						securityName, new HttpAuthenticationManagedObjectSource(httpSecurity));
				httpAuthenticationMos.setTimeout(securityTimeout);
				this.officeArchitect.link(httpAuthenticationMos.getManagedObjectFlow("AUTHENTICATE"),
						httpSecurity.getOfficeSection().getOfficeSectionInput("ManagedObjectAuthenticate"));
				this.officeArchitect.link(httpAuthenticationMos.getManagedObjectFlow("LOGOUT"),
						httpSecurity.getOfficeSection().getOfficeSectionInput("ManagedObjectLogout"));
				httpAuthenticationMos.addOfficeManagedObject(securityName, ManagedObjectScope.PROCESS);

				// Add the HTTP Security Managed Object
				String httpSecurityName = securityName + "Security";
				OfficeManagedObjectSource httpSecurityMos = this.officeArchitect
						.addOfficeManagedObjectSource(httpSecurityName, new HttpSecurityManagedObjectSource());
				httpSecurityMos.setTimeout(securityTimeout);
				httpSecurityMos.addProperty(HttpSecurityManagedObjectSource.PROPERTY_HTTP_SECURITY_TYPE,
						securityClass.getName());
				httpSecurityMos.addOfficeManagedObject(httpSecurityName, ManagedObjectScope.PROCESS);
			}
		}

		// Chain the servicers
		OfficeSectionOutput previousChainedOutput = httpSection
				.getOfficeSectionOutput(WebApplicationSectionSource.UNHANDLED_REQUEST_OUTPUT_NAME);
		for (ChainedServicer chainedServicer : this.chainedServicers) {

			// Link the chained servicer (if previous chained output)
			if (previousChainedOutput != null) {
				this.officeArchitect.link(previousChainedOutput, chainedServicer.sectionInput);
			}

			// Configure for next in chain
			previousChainedOutput = chainedServicer.notHandledOutput;
		}

		// End chain with file sender servicer (if previous chained output)
		if (previousChainedOutput != null) {

			// Create the filer sender servicer
			OfficeSection nonHandledServicer = this.officeArchitect.addOfficeSection("NON_HANDLED_SERVICER",
					HttpFileSenderSectionSource.class.getName(), null);
			SourceHttpResourceFactory.copyProperties(this.officeSourceContext, nonHandledServicer);
			this.linkToSendResponse(
					nonHandledServicer.getOfficeSectionOutput(HttpFileSenderSectionSource.FILE_SENT_OUTPUT_NAME));

			// Link into the chain
			this.officeArchitect.link(previousChainedOutput,
					nonHandledServicer.getOfficeSectionInput(HttpFileSenderSectionSource.SERVICE_INPUT_NAME));
		}

		// Additional template configuration
		NEXT_HTTP_TEMPLATE: for (HttpTemplateSectionImpl httpTemplate : this.httpTemplates) {

			// Determine the template inheritance hierarchy
			Deque<HttpTemplateSectionImpl> inheritanceHierarchy = new LinkedList<>();
			HttpTemplateSection parent = httpTemplate.getSuperHttpTemplate();
			boolean isCyclicInheritance = false;
			INHERITANCE: while ((parent != null) && (!isCyclicInheritance)) {

				// Issue if not template
				if (!(parent instanceof HttpTemplateSectionImpl)) {
					this.officeArchitect.addIssue(parent.getOfficeSection().getOfficeSectionName()
							+ " must be configured via " + WebArchitect.class.getSimpleName());
					break INHERITANCE;
				}
				HttpTemplateSectionImpl parentTemplate = (HttpTemplateSectionImpl) parent;

				// Determine if cyclic inheritance
				if (inheritanceHierarchy.contains(parentTemplate)) {
					isCyclicInheritance = true;
				}

				// Add the parent and set up for next iteration
				inheritanceHierarchy.push(parentTemplate);
				parent = parentTemplate.getSuperHttpTemplate();
			}
			if (isCyclicInheritance) {
				// Provide issue of cyclic inheritance
				StringBuilder logCycle = new StringBuilder();
				logCycle.append("Template " + httpTemplate.getTemplateUri() + " has a cyclic inheritance hierarchy ( ");
				for (HttpTemplateSection section : inheritanceHierarchy) {
					logCycle.append(section.getOfficeSection().getOfficeSectionName() + " : ");
				}
				logCycle.append("... )");
				String logCycleMessage = logCycle.toString();
				this.officeArchitect.addIssue(logCycleMessage);
				continue NEXT_HTTP_TEMPLATE;
			}

			// Obtain inheritance hierarchy of templates (including current)
			Deque<HttpTemplateSectionImpl> templateInheritanceHierarchy = new LinkedList<>();
			StringBuilder inheritedTemplates = new StringBuilder();
			boolean isFirstParentTemplate = true;
			for (HttpTemplateSectionImpl parentTemplate : inheritanceHierarchy) {

				// Include the template in inheritance hierarchy
				templateInheritanceHierarchy.add(parentTemplate);

				// Add the template
				if (!isFirstParentTemplate) {
					inheritedTemplates.append(", ");
				}
				isFirstParentTemplate = false;
				inheritedTemplates.append(parentTemplate.getTemplateLocation());
			}
			templateInheritanceHierarchy.push(httpTemplate);

			// Provide inheritance hierarchy for template (must be at least 2)
			if (templateInheritanceHierarchy.size() > 1) {
				httpTemplate.getOfficeSection().addProperty(HttpTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES,
						inheritedTemplates.toString());
			}

			// Determine the Content-Type
			String contentType = httpTemplate.getTemplateContentType();
			if (contentType != null) {

				// Determine if require providing charset
				if (contentType.trim().toLowerCase().startsWith("text/")) {

					// Text, so obtain charset parameter (if provided)
					StringBuilder contentTypeBuilder = new StringBuilder();
					String[] contentTypeEntries = contentType.split(";");
					contentTypeBuilder.append(contentTypeEntries[0]);
					for (int i = 1; i < contentTypeEntries.length; i++) {
						String contentTypeParameter = contentTypeEntries[i];
						String[] parameterNameValues = contentTypeParameter.split("=");
						if ("charset".equalsIgnoreCase(parameterNameValues[0].trim())) {
							// Load the charset for the template
							StringBuilder charset = new StringBuilder();
							for (int c = 1; c < parameterNameValues.length; c++) {
								if (c > 1) {
									charset.append("=");
								}
								charset.append(parameterNameValues[c]);
							}
							httpTemplate.getOfficeSection().addProperty(HttpTemplateSectionSource.PROPERTY_CHARSET,
									charset.toString());

						} else {
							// Include the parameter (as is)
							contentTypeBuilder.append(";");
							contentTypeBuilder.append(contentTypeParameter);
						}
					}

					// Provide the content-type (without charset parameter)
					contentType = contentTypeBuilder.toString();
				}

				// Provide the content type
				httpTemplate.getOfficeSection().addProperty(HttpTemplateSectionSource.PROPERTY_CONTENT_TYPE,
						contentType);
			}

			// Determine if template is secure
			boolean isTemplateSecure = httpTemplate.isTemplateSecure();

			// Obtain the template URI Suffix
			String templateUriSuffix = this.getTemplateUriSuffix(httpTemplate);

			// Provide the template URI (and potential URL continuation)
			String templateUri = httpTemplate.getTemplateUri();
			httpTemplate.getOfficeSection().addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, templateUri);
			if (templateUriSuffix != null) {
				httpTemplate.getOfficeSection()
						.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI_SUFFIX, templateUriSuffix);
			}

			// Secure the template
			httpTemplate.getOfficeSection().addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_SECURE,
					String.valueOf(isTemplateSecure));

			// Secure the specific template links (following inheritance)
			Set<String> configuredLinks = new HashSet<String>();
			for (HttpTemplateSectionImpl currentTemplate : templateInheritanceHierarchy) {

				// Provide the links for the current template
				Map<String, Boolean> secureLinks = currentTemplate.getSecureLinks();
				NEXT_LINK: for (String link : secureLinks.keySet()) {

					// Ignore if already configured link (by child)
					if (configuredLinks.contains(link)) {
						continue NEXT_LINK;
					}
					configuredLinks.add(link);

					// Determine if link is secure
					Boolean isLinkSecure = secureLinks.get(link);

					// Configure the link secure for the template
					httpTemplate.getOfficeSection().addProperty(
							HttpTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + link,
							String.valueOf(isLinkSecure));
				}
			}

			// Render redirect HTTP methods
			String[] renderRedirectHttpMethods = httpTemplate.getRenderRedirectHttpMethods();
			if ((renderRedirectHttpMethods != null) && (renderRedirectHttpMethods.length > 0)) {

				// Create the listing of rendering redirect HTTP methods
				StringBuilder renderRedirectHttpMethodValue = new StringBuilder();
				boolean isFirstRenderRedirectHttpMethod = true;
				for (String renderRedirectHttpMethod : renderRedirectHttpMethods) {
					if (!isFirstRenderRedirectHttpMethod) {
						renderRedirectHttpMethodValue.append(", ");
					}
					isFirstRenderRedirectHttpMethod = false;
					renderRedirectHttpMethodValue.append(renderRedirectHttpMethod);
				}

				// Configure the property for render redirect HTTP methods
				httpTemplate.getOfficeSection().addProperty(
						HttpTemplateInitialManagedFunctionSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
						renderRedirectHttpMethodValue.toString());
			}

			// Link completion of template rendering
			this.linkToSendResponse(httpTemplate.getOfficeSection()
					.getOfficeSectionOutput(HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME));
		}

		// Link to resources
		if ((this.resourceLinks.size() > 0) || (this.escalationResources.size() > 0)) {

			// Create section to send resources
			OfficeSection section = this.officeArchitect.addOfficeSection("RESOURCES",
					HttpFileSectionSource.class.getName(), WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX);
			SourceHttpResourceFactory.copyProperties(this.officeSourceContext, section);

			// Link section outputs to the resources
			for (ResourceLink resourceLink : this.resourceLinks) {
				this.officeArchitect.link(resourceLink.sectionOutput,
						section.getOfficeSectionInput(resourceLink.resourcePath));
				section.addProperty(HttpFileSectionSource.PROPERTY_RESOURCE_PREFIX + resourceLink.resourcePath,
						resourceLink.resourcePath);
			}

			// Link escalations to the resources
			for (EscalationResource escalation : this.escalationResources) {
				OfficeEscalation officeEscalation = this.officeArchitect
						.addOfficeEscalation(escalation.escalationType.getName());
				this.officeArchitect.link(officeEscalation, section.getOfficeSectionInput(escalation.resourcePath));
				section.addProperty(HttpFileSectionSource.PROPERTY_RESOURCE_PREFIX + escalation.resourcePath,
						escalation.resourcePath);
			}
		}

		// Link sending the response
		for (SendLink link : this.sendLinks) {
			this.officeArchitect.link(link.sectionOutput,
					httpSection.getOfficeSectionInput(WebApplicationSectionSource.SEND_RESPONSE_INPUT_NAME));
		}
	}

	/**
	 * Resource link.
	 */
	private static class ResourceLink {

		/**
		 * {@link OfficeSectionOutput}.
		 */
		public final OfficeSectionOutput sectionOutput;

		/**
		 * Resource path.
		 */
		public final String resourcePath;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link OfficeSection}.
		 * @param outputName
		 *            Name of the {@link SectionOutput}.
		 * @param resourcePath
		 *            Resource path.
		 */
		public ResourceLink(OfficeSectionOutput sectionOutput, String resourcePath) {
			this.sectionOutput = sectionOutput;
			this.resourcePath = resourcePath;
		}
	}

	/**
	 * Resource to handle {@link Escalation}.
	 */
	private static class EscalationResource {

		/**
		 * {@link Escalation} type.
		 */
		public final Class<? extends Throwable> escalationType;

		/**
		 * Resource path.
		 */
		public final String resourcePath;

		/**
		 * Initiate.
		 * 
		 * @param escalationType
		 *            {@link Escalation} type.
		 * @param resourcePath
		 *            Resource path.
		 */
		public EscalationResource(Class<? extends Throwable> escalationType, String resourcePath) {
			this.escalationType = escalationType;
			this.resourcePath = resourcePath;
		}
	}

	/**
	 * Send link.
	 */
	private static class SendLink {

		/**
		 * {@link OfficeSectionOutput}.
		 */
		public final OfficeSectionOutput sectionOutput;

		/**
		 * Initiate.
		 * 
		 * @param sectionOutput
		 *            {@link OfficeSectionOutput}.
		 */
		public SendLink(OfficeSectionOutput sectionOutput) {
			this.sectionOutput = sectionOutput;
		}
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

}