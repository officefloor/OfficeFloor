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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.office.ManagedFunctionAugmentorContext;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.HttpRouteSectionSource.RouteInput;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.build.HttpContentParametersBuilder;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpInputBuilder;
import net.officefloor.web.build.HttpObjectParser;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.HttpParameterBuilder;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.session.HttpSessionManagedObjectSource;
import net.officefloor.web.session.object.HttpSessionObjectManagedObjectSource;
import net.officefloor.web.state.HttpApplicationObjectManagedObjectSource;
import net.officefloor.web.state.HttpApplicationStateManagedObjectSource;
import net.officefloor.web.state.HttpArgumentManagedObjectSource;
import net.officefloor.web.state.HttpObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;
import net.officefloor.web.state.ObjectResponseManagedObjectSource;
import net.officefloor.web.tokenise.FormHttpArgumentParser;

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
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext officeSourceContext;

	/**
	 * Registry of HTTP arguments to its {@link OfficeManagedObject}.
	 */
	private final Map<String, OfficeManagedObject> httpArguments = new HashMap<>();

	/**
	 * Singleton {@link List} provided to the
	 * {@link HttpObjectManagedObjectSource} for the registered
	 * {@link HttpObjectParserFactory} instances.
	 */
	private final List<HttpObjectParserFactory> singletonObjectParserList = new LinkedList<>();

	/**
	 * Registry of {@link HttpObject} {@link Annotation} alias to accepted
	 * <code>content-type</code> values. Note: the keys indicate the aliases, as
	 * accepted <code>content-type</code> values are optional.
	 */
	private final Map<Class<?>, String[]> httpObjectAliases = new HashMap<>();

	/**
	 * Registry of HTTP objects by their {@link Class}.
	 */
	private final Map<Class<?>, OfficeManagedObject> httpObjects = new HashMap<>();

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
	 * {@link HttpObjectResponderFactory} instances.
	 */
	private final List<HttpObjectResponderFactory> objectResponderFactories = new LinkedList<>();

	/**
	 * {@link HttpInputBuilderImpl} instances.
	 */
	private final List<HttpInputBuilderImpl> inputs = new LinkedList<>();

	/**
	 * {@link ChainedServicer} instances.
	 */
	private final List<ChainedServicer> chainedServicers = new LinkedList<>();

	/**
	 * Context path.
	 */
	private String contextPath = null;

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
	private static String getBindName(Class<?> objectClass, String bindName) {
		return (bindName == null ? objectClass.getName() : bindName);
	}

	/*
	 * ======================== WebArchitect =========================
	 */

	@Override
	public OfficeManagedObject addHttpArgument(String parameterName, HttpValueLocation location) {

		// Obtain the bind name
		String bindName = "HTTP_" + (location == null ? "ANY" : location.name()) + "_" + parameterName;
		OfficeManagedObject object = this.httpArguments.get(bindName);
		if (object != null) {
			return object; // return the already register object
		}

		// Not registered, so register
		OfficeManagedObjectSource mos = this.officeArchitect.addOfficeManagedObjectSource(bindName,
				new HttpArgumentManagedObjectSource(parameterName, location));
		object = mos.addOfficeManagedObject(bindName, ManagedObjectScope.PROCESS);
		this.httpArguments.put(bindName, object);

		// Return the object
		return object;
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
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	@Override
	public void addHttpObjectParser(HttpObjectParserFactory objectParserFactory) {
		this.singletonObjectParserList.add(objectParserFactory);
	}

	@Override
	public void addHttpObjectAnnotationAlias(Class<?> httpObjectAnnotationAliasClass, String... acceptedContentTypes) {
		this.httpObjectAliases.put(httpObjectAnnotationAliasClass, acceptedContentTypes);
	}

	@Override
	public OfficeManagedObject addHttpObject(Class<?> objectClass, String... acceptedContentTypes) {

		// Determine if already registered
		OfficeManagedObject object = this.httpObjects.get(objectClass);
		if (object == null) {

			// Not registered, so register
			OfficeManagedObjectSource mos = this.officeArchitect.addOfficeManagedObjectSource(objectClass.getName(),
					new HttpObjectManagedObjectSource<>(objectClass, acceptedContentTypes,
							this.singletonObjectParserList));
			object = mos.addOfficeManagedObject(objectClass.getName(), ManagedObjectScope.PROCESS);
			this.httpObjects.put(objectClass, object);
		}

		// Return the object
		return object;
	}

	@Override
	public void addHttpObjectResponder(HttpObjectResponderFactory objectResponderFactory) {
		this.objectResponderFactories.add(objectResponderFactory);
	}

	@Override
	public HttpUrlContinuation link(boolean isSecure, String applicationPath, OfficeSectionInput sectionInput) {
		HttpInputBuilderImpl continuation = new HttpInputBuilderImpl(isSecure, applicationPath, sectionInput);
		this.inputs.add(continuation);
		return continuation;
	}

	@Override
	public HttpInputBuilder link(boolean isSecure, HttpMethod httpMethod, String applicationPath,
			OfficeSectionInput sectionInput) {
		HttpInputBuilderImpl input = new HttpInputBuilderImpl(isSecure, httpMethod, applicationPath, sectionInput);
		this.inputs.add(input);
		return input;
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

	@Override
	public void chainServicer(OfficeSectionInput sectionInput, OfficeSectionOutput notHandledOutput) {
		this.chainedServicers.add(new ChainedServicer(sectionInput, notHandledOutput));
	}

	@Override
	public void informOfficeArchitect() {

		// Auto wire the objects
		this.officeArchitect.enableAutoWireObjects();

		// Configure HTTP Session (allowing 10 seconds to retrieve session)
		OfficeManagedObjectSource httpSessionMos = this.officeArchitect.addOfficeManagedObjectSource("HTTP_SESSION",
				HttpSessionManagedObjectSource.class.getName());
		httpSessionMos.setTimeout(10 * 1000); // TODO make configurable
		httpSessionMos.addOfficeManagedObject("HTTP_SESSION", ManagedObjectScope.PROCESS);

		// Load the argument parsers
		HttpArgumentParser[] argumentParsers = new HttpArgumentParser[] { new FormHttpArgumentParser() };

		// Configure the HTTP Application and Request State
		this.officeArchitect
				.addOfficeManagedObjectSource("HTTP_APPLICATION_STATE",
						new HttpApplicationStateManagedObjectSource(this.contextPath))
				.addOfficeManagedObject("HTTP_APPLICATION_STATE", ManagedObjectScope.PROCESS);
		this.officeArchitect
				.addOfficeManagedObjectSource("HTTP_REQUEST_STATE",
						new HttpRequestStateManagedObjectSource(argumentParsers))
				.addOfficeManagedObject("HTTP_REQUEST_STATE", ManagedObjectScope.PROCESS);

		// Configure the object responder (if configured factories)
		if (this.objectResponderFactories.size() > 0) {
			this.officeArchitect
					.addOfficeManagedObjectSource("OBJECT_RESPONSE",
							new ObjectResponseManagedObjectSource(this.objectResponderFactories))
					.addOfficeManagedObject("OBJECT_RESPONSE", ManagedObjectScope.PROCESS);
		}

		// Configure the HTTP handler
		HttpRouteSectionSource routing = new HttpRouteSectionSource(this.contextPath);
		OfficeSection routingSection = this.officeArchitect.addOfficeSection(HANDLER_SECTION_NAME, routing, null);
		for (HttpInputBuilderImpl input : this.inputs) {

			// Add the route
			RouteInput routeInput = routing.addRoute(input.method, input.applicationPath);

			// Link route output to handling section input
			OfficeSectionOutput routeOutput = routingSection.getOfficeSectionOutput(routeInput.getOutputName());
			this.officeArchitect.link(routeOutput, input.sectionInput);
		}

		// Load in-line configured dependencies
		final Set<Class<?>> httpParameters = new HashSet<>();
		this.officeArchitect.addManagedFunctionAugmentor((context) -> {
			ManagedFunctionType<?, ?> functionType = context.getManagedFunctionType();
			for (ManagedFunctionObjectType<?> functionParameterType : functionType.getObjectTypes()) {
				Class<?> objectType = functionParameterType.getObjectType();

				// Determine if in-line configuration of dependency
				for (Object annotation : functionParameterType.getAnnotations()) {

					// HTTP parameters
					if (annotation instanceof HttpParameters) {
						// Load as HTTP parameters (only once)
						if (!httpParameters.contains(objectType)) {
							this.addHttpRequestObject(objectType, true);
							httpParameters.add(objectType);
						}
					}

					// HTTP object
					if (annotation instanceof HttpObject) {
						HttpObject httpObject = (HttpObject) annotation;
						String[] acceptedContentTypes = httpObject.acceptedContentTypes();
						this.addHttpObject(objectType, acceptedContentTypes);
					}

					// Determine if HTTP object alias annotation
					String[] acceptedContentTypes = WebArchitectEmployer.this.httpObjectAliases
							.get(annotation instanceof Annotation ? ((Annotation) annotation).annotationType()
									: annotation.getClass());
					if (acceptedContentTypes != null) {
						this.addHttpObject(objectType, acceptedContentTypes);
					}

					// Load HTTP arguments
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpPathParameter.class,
							HttpValueLocation.PATH, objectType, context, (parameter) -> parameter.value(),
							(parameter) -> new HttpPathParameter.HttpPathParameterNameFactory()
									.getQualifierName(parameter));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpQueryParameter.class,
							HttpValueLocation.QUERY, objectType, context, (parameter) -> parameter.value(),
							(parameter) -> new HttpQueryParameter.HttpQueryParameterNameFactory()
									.getQualifierName(parameter));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpHeaderParameter.class,
							HttpValueLocation.HEADER, objectType, context, (parameter) -> parameter.value(),
							(parameter) -> new HttpHeaderParameter.HttpHeaderParameterNameFactory()
									.getQualifierName(parameter));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpCookieParameter.class,
							HttpValueLocation.COOKIE, objectType, context, (parameter) -> parameter.value(),
							(parameter) -> new HttpCookieParameter.HttpCookieParameterNameFactory()
									.getQualifierName(parameter));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpContentParameter.class,
							HttpValueLocation.ENTITY, objectType, context, (parameter) -> parameter.value(),
							(parameter) -> new HttpContentParameter.HttpContentParameterNameFactory()
									.getQualifierName(parameter));
				}
			}
		});

		// Configure not handled
		this.officeArchitect.link(routingSection.getOfficeSectionOutput(HttpRouteSectionSource.UNHANDLED_OUTPUT_NAME),
				routingSection.getOfficeSectionInput(HttpRouteSectionSource.NOT_FOUND_INPUT_NAME));
	}

	/**
	 * Loads the in-line HTTP argument.
	 * 
	 * @param annotation
	 *            {@link Annotation}.
	 * @param annotationType
	 *            Type of {@link Annotation}.
	 * @param valueLocation
	 *            {@link HttpValueLocation}.
	 * @param objectType
	 *            Parameter object type.
	 * @param context
	 *            {@link ManagedFunctionAugmentorContext}.
	 * @param getParameterName
	 *            {@link Function} to obtain the parameter name.
	 * @param getQualifierName
	 *            {@link Function} to obtain the type qualification name.
	 */
	private <P extends Annotation> void loadInlineHttpArgument(Object annotation, Class<P> annotationType,
			HttpValueLocation valueLocation, Class<?> objectType, ManagedFunctionAugmentorContext context,
			Function<P, String> getParameterName, Function<P, String> getQualifierName) {

		// Ensure appropriate annotation
		if (!(annotation instanceof Annotation)) {
			return;
		}
		if (((Annotation) annotation).annotationType() != annotationType) {
			return;
		}

		// Obtain the parameter
		@SuppressWarnings("unchecked")
		P parameterAnnotation = (P) annotation;

		// Ensure parameter object is a String
		if (objectType != String.class) {
			this.officeArchitect.addIssue("Parameter must be " + String.class.getName() + " but was "
					+ objectType.getName() + " for function " + context.getManagedFunctionName());
		}

		// Add the HTTP argument
		String parameterName = getParameterName.apply(parameterAnnotation);
		String typeQualifier = getQualifierName.apply(parameterAnnotation);
		this.addHttpArgument(parameterName, valueLocation).addTypeQualification(typeQualifier, String.class.getName());
	}

	/**
	 * {@link HttpInputBuilder} implementation.
	 */
	private static class HttpInputBuilderImpl implements HttpInputBuilder, HttpUrlContinuation {

		/**
		 * Indicates if {@link HttpUrlContinuation}.
		 */
		private final boolean isUrlContinuation;

		/**
		 * Indicates if secure.
		 */
		private final boolean isSecure;

		/**
		 * {@link HttpMethod}.
		 */
		private final HttpMethod method;

		/**
		 * Application path.
		 */
		private final String applicationPath;

		/**
		 * Handling {@link OfficeSectionInput}.
		 */
		private final OfficeSectionInput sectionInput;

		/**
		 * Instantiate as {@link HttpUrlContinuation}.
		 * 
		 * @param isSecure
		 *            Indicates if secure.
		 * @param applicationPath
		 *            Application path.
		 * @param sectionInput
		 *            Handling {@link OfficeSectionInput}.
		 */
		public HttpInputBuilderImpl(boolean isSecure, String applicationPath, OfficeSectionInput sectionInput) {
			this.isUrlContinuation = true;
			this.isSecure = isSecure;
			this.method = HttpMethod.GET;
			this.applicationPath = applicationPath;
			this.sectionInput = sectionInput;
		}

		/**
		 * Instantiate as {@link HttpInputBuilder}.
		 * 
		 * @param isSecure
		 *            Indicates if secure.
		 * @param method
		 *            {@link HttpMethod}.
		 * @param applicationPath
		 *            Application path.
		 * @param sectionInput
		 *            Handling {@link OfficeSectionInput}.
		 */
		public HttpInputBuilderImpl(boolean isSecure, HttpMethod method, String applicationPath,
				OfficeSectionInput sectionInput) {
			this.isUrlContinuation = false;
			this.isSecure = isSecure;
			this.method = method;
			this.applicationPath = applicationPath;
			this.sectionInput = sectionInput;
		}

		/*
		 * ================== HttpInputBuilder ====================
		 */

		@Override
		public HttpParameterBuilder addQueryParameter(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpParameterBuilder addHeaderParameter(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpParameterBuilder addCookieParameter(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpContentParametersBuilder addEntityParameters(HttpArgumentParser argumentParser) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addRequestObject(HttpObjectParser<?> objectParser) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addResponseContentType(String contentType) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addResponseObject(HttpObjectResponder<?> objectResponder) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addAnnotation(Object annotation) {
			// TODO Auto-generated method stub

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