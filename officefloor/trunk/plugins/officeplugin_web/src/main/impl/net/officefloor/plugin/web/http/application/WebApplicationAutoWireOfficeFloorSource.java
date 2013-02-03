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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.AutoWireSectionFactory;
import net.officefloor.autowire.AutoWireSectionTransformer;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationSectionSource;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationWorkSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.web.http.security.HttpAuthentication;
import net.officefloor.plugin.web.http.security.HttpAuthenticationManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecurityConfigurator;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySectionSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoader;
import net.officefloor.plugin.web.http.security.type.HttpSecurityLoaderImpl;
import net.officefloor.plugin.web.http.security.type.HttpSecurityType;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.session.object.HttpSessionObjectManagedObjectSource;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialWorkSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

/**
 * {@link AutoWireOfficeFloorSource} providing web application functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class WebApplicationAutoWireOfficeFloorSource extends
		AutoWireOfficeFloorSource implements WebAutoWireApplication {

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
			sectionName = (templateUri.startsWith("/") ? templateUri
					.substring("/".length()) : templateUri);
		}

		// Return the section name
		return sectionName;
	}

	/**
	 * Prefix for the link service {@link Task} name.
	 */
	private static final String LINK_SERVICE_TASK_NAME_PREFIX = "LINK_";

	/**
	 * {@link HttpTemplateAutoWireSection} instances.
	 */
	private final List<HttpTemplateAutoWireSection> httpTemplates = new LinkedList<HttpTemplateAutoWireSection>();

	/**
	 * Default {@link HttpTemplate} URI suffix.
	 */
	private String defaultTemplateUriSuffix = null;

	/**
	 * {@link HttpSecurityAutoWireSection} instance.
	 */
	private HttpSecurityAutoWireSection security = null;

	/**
	 * {@link HttpUrlContinuationSectionSource} to provide the
	 * {@link AutoWireSectionTransformer} for the configured URL continuations.
	 */
	private final HttpUrlContinuationSectionSource urlContinuations = new HttpUrlContinuationSectionSource();

	/**
	 * Registry of HTTP Application Object class to its {@link AutoWireObject}.
	 */
	private final Map<Class<?>, AutoWireObject> httpApplicationObjects = new HashMap<Class<?>, AutoWireObject>();

	/**
	 * Registry of HTTP Session Object class to its {@link AutoWireObject}.
	 */
	private final Map<Class<?>, AutoWireObject> httpSessionObjects = new HashMap<Class<?>, AutoWireObject>();

	/**
	 * Registry of HTTP Request Object class to its {@link AutoWireObject}.
	 */
	private final Map<Class<?>, AutoWireObject> httpRequestObjects = new HashMap<Class<?>, AutoWireObject>();

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
	 * Initiate.
	 */
	public WebApplicationAutoWireOfficeFloorSource() {
		// Configure in the auto wire section transformer for URL continuations
		this.addSectionTransformer(this.urlContinuations);
	}

	/**
	 * Obtains the {@link HttpTemplate} URI suffix.
	 * 
	 * @param template
	 *            {@link HttpTemplateAutoWireSection}.
	 * @return {@link HttpTemplate} URI suffix.
	 */
	private String getTemplateUriSuffix(HttpTemplateAutoWireSection template) {

		// Obtain the template URI Suffix
		String templateUriSuffix = template.getTemplateUriSuffix();
		if (templateUriSuffix == null) {
			templateUriSuffix = (this.defaultTemplateUriSuffix == null ? ""
					: this.defaultTemplateUriSuffix);
		}

		// Return the URI suffix
		return templateUriSuffix;
	}

	/*
	 * ======================== WebAutoWireApplication =========================
	 */

	@Override
	public HttpTemplateAutoWireSection addHttpTemplate(String templateUri,
			String templateFilePath, final Class<?> templateLogicClass) {

		// Obtain canonical template URI path
		try {
			templateUri = HttpUrlContinuationWorkSource
					.getApplicationUriPath(templateUri);
		} catch (InvalidHttpRequestUriException ex) {
			// Use the template URI as is
		}

		// Ensure URI is not already registered
		for (HttpTemplateAutoWireSection template : this.httpTemplates) {
			if (templateUri.equals(template.getTemplateUri())) {
				throw new IllegalStateException(
						"HTTP Template already added for URI '" + templateUri
								+ "'");
			}
		}

		// Determine section name from template URI
		String sectionName = getTemplateSectionName(templateUri);

		// Add the HTTP template section
		final String uriPath = templateUri;
		HttpTemplateAutoWireSection template = this.addSection(sectionName,
				HttpTemplateSectionSource.class.getName(), templateFilePath,
				new AutoWireSectionFactory<HttpTemplateAutoWireSection>() {
					@Override
					public HttpTemplateAutoWireSection createAutoWireSection(
							AutoWireSection seed) {
						// Create and return the template
						return new HttpTemplateAutoWireSectionImpl(
								WebApplicationAutoWireOfficeFloorSource.this
										.getOfficeFloorCompiler(), seed,
								templateLogicClass, uriPath);
					}
				});
		if (templateLogicClass != null) {
			template.addProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
					templateLogicClass.getName());
		}

		// Register the HTTP template
		this.httpTemplates.add(template);

		// Add the annotated parameters (if have template logic class)
		if (templateLogicClass != null) {
			for (Method method : templateLogicClass.getMethods()) {
				for (Class<?> parameterType : method.getParameterTypes()) {

					// HTTP Application Stateful
					HttpApplicationStateful applicationAnnotation = parameterType
							.getAnnotation(HttpApplicationStateful.class);
					if (applicationAnnotation != null) {
						this.addHttpApplicationObject(parameterType,
								applicationAnnotation.value());
					}

					// HTTP Session Stateful
					HttpSessionStateful sessionAnnotation = parameterType
							.getAnnotation(HttpSessionStateful.class);
					if (sessionAnnotation != null) {
						this.addHttpSessionObject(parameterType,
								sessionAnnotation.value());
					}

					// HTTP Request Stateful
					HttpRequestStateful requestAnnotation = parameterType
							.getAnnotation(HttpRequestStateful.class);
					if (requestAnnotation != null) {
						this.addHttpRequestObject(parameterType, false,
								requestAnnotation.value());
					}

					// HTTP Parameters
					HttpParameters parameters = parameterType
							.getAnnotation(HttpParameters.class);
					if (parameters != null) {
						this.addHttpRequestObject(parameterType, true,
								parameters.value());
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
	public HttpSecurityAutoWireSection setHttpSecurity(
			final Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass) {

		// Ensure security not already been specified
		if (this.security != null) {
			throw new IllegalStateException("HTTP Security already specified");
		}

		// Add the HTTP Security
		HttpSecurityAutoWireSection security = this.addSection("SECURITY",
				HttpSecuritySectionSource.class.getName(), null,
				new AutoWireSectionFactory<HttpSecurityAutoWireSection>() {
					@Override
					public HttpSecurityAutoWireSection createAutoWireSection(
							AutoWireSection seed) {
						// Create and return the template
						return new HttpSecurityAutoWireSectionImpl(
								WebApplicationAutoWireOfficeFloorSource.this
										.getOfficeFloorCompiler(), seed,
								httpSecuritySourceClass);
					}
				});

		// Specify the security
		this.security = security;

		// Return the security
		return this.security;
	}

	@Override
	public AutoWireObject addHttpApplicationObject(Class<?> objectClass,
			String bindName) {

		// Determine if already registered the type
		AutoWireObject object = this.httpApplicationObjects.get(objectClass);
		if (object != null) {
			return object; // return the already registered object
		}

		// Not registered, so register
		object = this.addManagedObject(
				HttpApplicationObjectManagedObjectSource.class.getName(), null,
				new AutoWire(objectClass));
		object.addProperty(
				HttpApplicationObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				objectClass.getName());
		if ((bindName != null) && (bindName.trim().length() > 0)) {
			object.addProperty(
					HttpApplicationObjectManagedObjectSource.PROPERTY_BIND_NAME,
					bindName);
		}
		this.httpApplicationObjects.put(objectClass, object);

		// Return the object
		return object;
	}

	@Override
	public AutoWireObject addHttpApplicationObject(Class<?> objectClass) {
		return this.addHttpApplicationObject(objectClass, null);
	}

	@Override
	public AutoWireObject addHttpSessionObject(Class<?> objectClass,
			String bindName) {

		// Determine if already registered the type
		AutoWireObject object = this.httpSessionObjects.get(objectClass);
		if (object != null) {
			return object; // return the already registered object
		}

		// Not registered, so register
		object = this.addManagedObject(
				HttpSessionObjectManagedObjectSource.class.getName(), null,
				new AutoWire(objectClass));
		object.addProperty(
				HttpSessionObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				objectClass.getName());
		if ((bindName != null) && (bindName.trim().length() > 0)) {
			object.addProperty(
					HttpSessionObjectManagedObjectSource.PROPERTY_BIND_NAME,
					bindName);
		}
		this.httpSessionObjects.put(objectClass, object);

		// Return the object
		return object;
	}

	@Override
	public AutoWireObject addHttpSessionObject(Class<?> objectClass) {
		return this.addHttpSessionObject(objectClass, null);
	}

	@Override
	public AutoWireObject addHttpRequestObject(Class<?> objectClass,
			boolean isLoadParameters, String bindName) {

		// Determine if already registered the type
		AutoWireObject object = this.httpRequestObjects.get(objectClass);
		if (object == null) {

			// Not registered, so register
			object = this.addManagedObject(
					HttpRequestObjectManagedObjectSource.class.getName(), null,
					new AutoWire(objectClass));
			object.addProperty(
					HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME,
					objectClass.getName());
			if ((bindName != null) && (bindName.trim().length() > 0)) {
				object.addProperty(
						HttpRequestObjectManagedObjectSource.PROPERTY_BIND_NAME,
						bindName);
			}
			this.httpRequestObjects.put(objectClass, object);
		}

		// Determine if load HTTP parameters
		if (isLoadParameters) {

			// Obtain the load HTTP parameters property
			Property loadParametersProperty = null;
			for (Property property : object.getProperties()) {
				if (HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS
						.equals(property.getName())) {
					loadParametersProperty = property;
				}
			}

			// Flag to load parameters
			if (loadParametersProperty == null) {
				// Add the property to load parameters
				object.addProperty(
						HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
						String.valueOf(true));
			} else {
				// Ensure flagged to load parameters
				loadParametersProperty.setValue(String.valueOf(true));
			}
		}

		// Return the object
		return object;
	}

	@Override
	public AutoWireObject addHttpRequestObject(Class<?> objectClass,
			boolean isLoadParameters) {
		return this.addHttpRequestObject(objectClass, isLoadParameters, null);
	}

	@Override
	public HttpUriLink linkUri(String uri, AutoWireSection section,
			String inputName) {
		return this.urlContinuations.linkUri(uri, section, inputName);
	}

	@Override
	public void linkToHttpTemplate(AutoWireSection section, String outputName,
			HttpTemplateAutoWireSection template) {
		this.link(section, outputName, template,
				HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);
	}

	@Override
	public void linkToResource(AutoWireSection section, String outputName,
			String resourcePath) {
		this.resourceLinks.add(new ResourceLink(section, outputName,
				resourcePath));
	}

	@Override
	public void linkEscalation(Class<? extends Throwable> escalation,
			HttpTemplateAutoWireSection template) {
		this.linkEscalation(escalation, template,
				HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);
	}

	@Override
	public void linkEscalation(Class<? extends Throwable> escalation,
			String resourcePath) {
		this.escalationResources.add(new EscalationResource(escalation,
				resourcePath));
	}

	@Override
	public void linkToSendResponse(AutoWireSection section, String outputName) {
		this.sendLinks.add(new SendLink(section, outputName));
	}

	@Override
	public void chainServicer(AutoWireSection section, String inputName,
			String notHandledOutputName) {
		this.chainedServicers.add(new ChainedServicer(section, inputName,
				notHandledOutputName));
	}

	@Override
	public String[] getURIs() {

		// Create the URIs
		List<String> uris = new LinkedList<String>();

		// Determine if root template to include
		for (HttpTemplateAutoWireSection template : this.httpTemplates) {
			String templateUri = template.getTemplateUri();
			if ("/".equals(templateUri)) {
				uris.add(templateUri);
			}
		}

		// Add the linked URIs
		for (HttpUriLink link : this.urlContinuations
				.getRegisteredHttpUriLinks()) {

			// Obtain the URI path
			String uriPath = link.getApplicationUriPath();
			try {
				uriPath = HttpUrlContinuationWorkSource
						.getApplicationUriPath(uriPath);
			} catch (InvalidHttpRequestUriException ex) {
				// Do nothing and keep URI path as is
			}

			// Add the URI path
			uris.add(uriPath);
		}

		// Return the URIs
		return uris.toArray(new String[uris.size()]);
	}

	/*
	 * ===================== AutoWireOfficeFloorSource =======================
	 */

	@Override
	protected void initOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Add the HTTP section
		AutoWireSection httpSection = this.addSection(HANDLER_SECTION_NAME,
				WebApplicationSectionSource.class.getName(), null);
		httpSection
				.addProperty(
						WebApplicationSectionSource.PROPERTY_LINK_SERVICE_TASK_NAME_PREFIX,
						LINK_SERVICE_TASK_NAME_PREFIX);

		// Ensure have HTTP Application Location
		AutoWire locationAutoWire = new AutoWire(HttpApplicationLocation.class);
		if (!(this.isObjectAvailable(locationAutoWire))) {
			// Add the HTTP Application Location
			AutoWireObject location = this.addManagedObject(
					HttpApplicationLocationManagedObjectSource.class.getName(),
					null, locationAutoWire);
			HttpApplicationLocationManagedObjectSource.copyProperties(context,
					location);
		}

		// Load the HTTP security
		if (this.security != null) {

			// Configure the security
			Class<? extends HttpSecuritySource<?, ?, ?, ?>> httpSecuritySourceClass = this.security
					.getHttpSecuritySourceClass();

			// Load the HTTP security source
			HttpSecuritySource<?, ?, ?, ?> httpSecuritySource = (HttpSecuritySource<?, ?, ?, ?>) context
					.loadClass(httpSecuritySourceClass.getName()).newInstance();

			// Load the type (which also initialises the source)
			HttpSecurityLoader securityLoader = new HttpSecurityLoaderImpl(
					context);
			HttpSecurityType httpSecurityType = securityLoader
					.loadHttpSecurityType(httpSecuritySource,
							this.security.getProperties());

			// Obtain the security class
			Class<?> securityClass = httpSecurityType.getSecurityClass();

			// Register the HTTP security
			String key = HttpSecurityConfigurator.registerHttpSecuritySource(
					httpSecuritySource, httpSecurityType);
			this.security
					.addProperty(
							HttpSecuritySectionSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY,
							key);

			// Provide automated flow linking
			this.link(this.security, "Recontinue", httpSection,
					HANDLER_INPUT_NAME);

			// Add the HTTP Authentication Managed Object
			AutoWireObject httpAuthentication = this.addManagedObject(
					HttpAuthenticationManagedObjectSource.class.getName(),
					null, new AutoWire(HttpAuthentication.class));
			httpAuthentication
					.addProperty(
							HttpAuthenticationManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_KEY,
							key);

			// Add the HTTP Security Managed Object
			AutoWireObject httpSecurity = this.addManagedObject(
					HttpSecurityManagedObjectSource.class.getName(), null,
					new AutoWire(securityClass));
			httpSecurity
					.addProperty(
							HttpSecurityManagedObjectSource.PROPERTY_HTTP_SECURITY_TYPE,
							securityClass.getName());
		}

		// Chain the servicers
		AutoWireSection previousChainedSection = httpSection;
		String previousChainedOutput = WebApplicationSectionSource.UNHANDLED_REQUEST_OUTPUT_NAME;
		for (ChainedServicer chainedServicer : this.chainedServicers) {

			// Link the chained servicer (if previous chained output)
			if (previousChainedOutput != null) {
				this.link(previousChainedSection, previousChainedOutput,
						chainedServicer.section, chainedServicer.inputName);
			}

			// Configure for next in chain
			previousChainedSection = chainedServicer.section;
			previousChainedOutput = chainedServicer.notHandledOutputName;
		}

		// End chain with file sender servicer (if previous chained output)
		if (previousChainedOutput != null) {

			// Create the filer sender servicer
			AutoWireSection nonHandledServicer = this.addSection(
					"NON_HANDLED_SERVICER",
					HttpFileSenderSectionSource.class.getName(), null);
			SourceHttpResourceFactory.copyProperties(context,
					nonHandledServicer);
			this.linkToSendResponse(nonHandledServicer,
					HttpFileSenderSectionSource.FILE_SENT_OUTPUT_NAME);

			// Link into the chain
			this.link(previousChainedSection, previousChainedOutput,
					nonHandledServicer,
					HttpFileSenderSectionSource.SERVICE_INPUT_NAME);
		}

		// Additional template configuration
		for (HttpTemplateAutoWireSection httpTemplate : this.httpTemplates) {

			// Determine if template is secure
			boolean isTemplateSecure = httpTemplate.isTemplateSecure();

			// Obtain the template URI Suffix
			String templateUriSuffix = this.getTemplateUriSuffix(httpTemplate);

			// Provide the template URI (and potential URL continuation)
			String templateUri = httpTemplate.getTemplateUri();
			if (templateUri == null) {
				// Use section name and keep private (no URL continuation)
				templateUri = httpTemplate.getSectionName();
			}
			httpTemplate.addProperty(
					HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI,
					templateUri);
			if (templateUriSuffix != null) {
				httpTemplate.addProperty(
						HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI_SUFFIX,
						templateUriSuffix);
			}

			// Secure the template
			httpTemplate.addProperty(
					HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
					String.valueOf(isTemplateSecure));

			// Secure the specific template links
			Map<String, Boolean> secureLinks = httpTemplate.getSecureLinks();
			for (String link : secureLinks.keySet()) {
				Boolean isLinkSecure = secureLinks.get(link);

				// Configure the link secure for the template
				httpTemplate.addProperty(
						HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX
								+ link, String.valueOf(isLinkSecure));
			}

			// Render redirect HTTP methods
			String[] renderRedirectHttpMethods = httpTemplate
					.getRenderRedirectHttpMethods();
			if ((renderRedirectHttpMethods != null)
					&& (renderRedirectHttpMethods.length > 0)) {

				// Create the listing of rendering redirect HTTP methods
				StringBuilder renderRedirectHttpMethodValue = new StringBuilder();
				boolean isFirst = true;
				for (String renderRedirectHttpMethod : renderRedirectHttpMethods) {
					if (!isFirst) {
						renderRedirectHttpMethodValue.append(", ");
					}
					isFirst = false;
					renderRedirectHttpMethodValue
							.append(renderRedirectHttpMethod);
				}

				// Configure the property for render redirect HTTP methods
				httpTemplate
						.addProperty(
								HttpTemplateInitialWorkSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
								renderRedirectHttpMethodValue.toString());
			}

			// Link completion of template rendering (if not already linked)
			if (!this.isLinked(httpTemplate,
					HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME)) {
				// Not linked, so link to sending HTTP response
				this.linkToSendResponse(httpTemplate,
						HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME);
			}
		}

		// Link to resources
		if ((this.resourceLinks.size() > 0)
				|| (this.escalationResources.size() > 0)) {

			// Create section to send resources
			AutoWireSection section = this.addSection("RESOURCES",
					HttpFileSectionSource.class.getName(),
					WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX);
			SourceHttpResourceFactory.copyProperties(context, section);

			// Link section outputs to the resources
			for (ResourceLink resourceLink : this.resourceLinks) {
				this.link(resourceLink.section, resourceLink.outputName,
						section, resourceLink.resourcePath);
				section.addProperty(
						HttpFileSectionSource.PROPERTY_RESOURCE_PREFIX
								+ resourceLink.resourcePath,
						resourceLink.resourcePath);
			}

			// Link escalations to the resources
			for (EscalationResource escalation : this.escalationResources) {
				this.linkEscalation(escalation.escalationType, section,
						escalation.resourcePath);
				section.addProperty(
						HttpFileSectionSource.PROPERTY_RESOURCE_PREFIX
								+ escalation.resourcePath,
						escalation.resourcePath);
			}
		}

		// Link sending the response
		for (SendLink link : this.sendLinks) {
			this.link(link.section, link.outputName, httpSection,
					WebApplicationSectionSource.SEND_RESPONSE_INPUT_NAME);
		}
	}

	/**
	 * Resource link.
	 */
	private static class ResourceLink {

		/**
		 * {@link AutoWireSection}.
		 */
		public final AutoWireSection section;

		/**
		 * Name of the {@link SectionOutput}.
		 */
		public final String outputName;

		/**
		 * Resource path.
		 */
		public final String resourcePath;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link AutoWireSection}.
		 * @param outputName
		 *            Name of the {@link SectionOutput}.
		 * @param resourcePath
		 *            Resource path.
		 */
		public ResourceLink(AutoWireSection section, String outputName,
				String resourcePath) {
			this.section = section;
			this.outputName = outputName;
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
		public EscalationResource(Class<? extends Throwable> escalationType,
				String resourcePath) {
			this.escalationType = escalationType;
			this.resourcePath = resourcePath;
		}
	}

	/**
	 * Send link.
	 */
	private static class SendLink {

		/**
		 * {@link AutoWireSection}.
		 */
		public final AutoWireSection section;

		/**
		 * Name of the {@link SectionOutput}.
		 */
		public final String outputName;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link AutoWireSection}.
		 * @param outputName
		 *            Name of the {@link SectionOutput}.
		 */
		public SendLink(AutoWireSection section, String outputName) {
			this.section = section;
			this.outputName = outputName;
		}
	}

	/**
	 * Chained servicer.
	 */
	private static class ChainedServicer {

		/**
		 * {@link AutoWireSection}.
		 */
		public final AutoWireSection section;

		/**
		 * Name of the {@link SectionInput}.
		 */
		public final String inputName;

		/**
		 * Name of the {@link SectionOutput}.
		 */
		public final String notHandledOutputName;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link AutoWireSection}.
		 * @param inputName
		 *            Name of the {@link SectionInput}.
		 * @param notHandledOutputName
		 *            Name of the {@link SectionOutput}. May be
		 *            <code>null</code>.
		 */
		public ChainedServicer(AutoWireSection section, String inputName,
				String notHandledOutputName) {
			this.section = section;
			this.inputName = inputName;
			this.notHandledOutputName = notHandledOutputName;
		}
	}

}