/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.ManagedFunctionAugmentorContext;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpRouteSectionSource.Interception;
import net.officefloor.web.HttpRouteSectionSource.Redirect;
import net.officefloor.web.HttpRouteSectionSource.RouteInput;
import net.officefloor.web.accept.AcceptNegotiatorBuilderImpl;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpInputExplorer;
import net.officefloor.web.build.HttpInputExplorerContext;
import net.officefloor.web.build.HttpObjectParserFactory;
import net.officefloor.web.build.HttpObjectParserServiceFactory;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.build.HttpObjectResponderServiceFactory;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.build.WebInterceptServiceFactory;
import net.officefloor.web.response.ObjectResponseManagedObjectSource;
import net.officefloor.web.response.ObjectResponseManagedObjectSource.DefaultHttpObjectResponder;
import net.officefloor.web.route.WebRouterBuilder;
import net.officefloor.web.session.HttpSessionManagedObjectSource;
import net.officefloor.web.session.object.HttpSessionObjectManagedObjectSource;
import net.officefloor.web.state.HttpApplicationObjectManagedObjectSource;
import net.officefloor.web.state.HttpApplicationStateManagedObjectSource;
import net.officefloor.web.state.HttpArgumentManagedObjectSource;
import net.officefloor.web.state.HttpObjectManagedObjectSource;
import net.officefloor.web.state.HttpObjectManagedObjectSource.DefaultHttpObjectParser;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;
import net.officefloor.web.tokenise.FormHttpArgumentParser;

/**
 * {@link WebArchitect} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebArchitectEmployer implements WebArchitect {

	/**
	 * Name of {@link Property} specifying the context path.
	 */
	public static final String PROPERTY_CONTEXT_PATH = "context.path";

	/**
	 * Employs a {@link WebArchitect}.
	 * 
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext} used to source
	 *                            {@link Property} values to configure the
	 *                            {@link WebArchitect}.
	 * @return {@link WebArchitect}.
	 */
	public static WebArchitect employWebArchitect(OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {

		// Obtain the context path
		String contextPath = officeSourceContext.getProperty(PROPERTY_CONTEXT_PATH, null);

		// Employ the web architect
		return employWebArchitect(contextPath, officeArchitect, officeSourceContext);
	}

	/**
	 * Employs a {@link WebArchitect}.
	 * 
	 * @param contextPath         Context path for the web application. May be
	 *                            <code>null</code> for no context path.
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext} used to source
	 *                            {@link Property} values to configure the
	 *                            {@link WebArchitect}.
	 * @return {@link WebArchitect}.
	 */
	public static WebArchitect employWebArchitect(String contextPath, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		return new WebArchitectEmployer(contextPath, officeArchitect, officeSourceContext);
	}

	/**
	 * Context path. May be <code>null</code> for no context path.
	 */
	private final String contextPath;

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext officeSourceContext;

	/**
	 * {@link HttpRouteSectionSource}.
	 */
	private final HttpRouteSectionSource routing;

	/**
	 * Routing {@link OfficeSection}.
	 */
	private final OfficeSection routingSection;

	/**
	 * Registry of HTTP arguments to its {@link OfficeManagedObject}.
	 */
	private final Map<String, OfficeManagedObject> httpArguments = new HashMap<>();

	/**
	 * Singleton {@link List} provided to the {@link HttpObjectManagedObjectSource}
	 * for the registered {@link HttpObjectParserFactory} instances.
	 */
	private final List<HttpObjectParserFactory> singletonObjectParserList = new LinkedList<>();

	/**
	 * Default {@link HttpObjectParserServiceFactory}.
	 */
	private HttpObjectParserServiceFactory defaultHttpObjectParserServiceFactory = null;

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
	 * {@link HttpInputExplorer} instances.
	 */
	private final List<HttpInputExplorer> httpInputExplorers = new LinkedList<>();

	/**
	 * Default {@link HttpObjectResponderServiceFactory}.
	 */
	private HttpObjectResponderServiceFactory defaultHttpObjectResponderServiceFactory = null;

	/**
	 * {@link HttpInputImpl} instances.
	 */
	private final List<HttpInputImpl> inputs = new LinkedList<>();

	/**
	 * {@link Interceptor} instances.
	 */
	private final List<Interceptor> interceptors = new LinkedList<>();

	/**
	 * {@link ChainedServicer} instances.
	 */
	private final List<ChainedServicer> chainedServicers = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param contextPath         Context path for the web application. May be
	 *                            <code>null</code> for no context path.
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext}.
	 */
	private WebArchitectEmployer(String contextPath, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		this.contextPath = contextPath;
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;
		this.routing = new HttpRouteSectionSource(this.contextPath);
		this.routingSection = this.officeArchitect.addOfficeSection(HANDLER_SECTION_NAME, this.routing, null);

		// Obtain the registered HTTP object parser factories
		Iterable<HttpObjectParserFactory> objectParserFactories = this.officeSourceContext
				.loadOptionalServices(HttpObjectParserServiceFactory.class);
		for (HttpObjectParserFactory objectParserFactory : objectParserFactories) {
			this.singletonObjectParserList.add(objectParserFactory);
		}

		// Obtain the HTTP object responder factories
		Iterable<HttpObjectResponderFactory> objectResponderFactories = this.officeSourceContext
				.loadOptionalServices(HttpObjectResponderServiceFactory.class);
		for (HttpObjectResponderFactory objectResponderFactory : objectResponderFactories) {
			this.objectResponderFactories.add(objectResponderFactory);
		}
	}

	/**
	 * Obtains the bind name for the {@link OfficeManagedObject}.
	 * 
	 * @param objectClass {@link Class} of the {@link Object}.
	 * @param bindName    Optional bind name. May be <code>null</code>.
	 * @return Bind name for the {@link OfficeManagedObject};
	 */
	private static String getBindName(Class<?> objectClass, String bindName) {
		return (CompileUtil.isBlank(bindName) ? objectClass.getName() : bindName);
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
		OfficeManagedObject object = this.httpSessionObjects.get(bindName);
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
	public void addHttpObjectParser(HttpObjectParserFactory objectParserFactory) {
		this.singletonObjectParserList.add(objectParserFactory);
	}

	@Override
	public void setDefaultHttpObjectParser(HttpObjectParserServiceFactory objectParserServiceFactory) {
		this.defaultHttpObjectParserServiceFactory = objectParserServiceFactory;
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

			// Create the default object parser
			DefaultHttpObjectParser defaultHttpObjectParser = () -> this.defaultHttpObjectParserServiceFactory != null
					? this.officeSourceContext.loadService(this.defaultHttpObjectParserServiceFactory)
					: null;

			// Not registered, so register
			OfficeManagedObjectSource mos = this.officeArchitect.addOfficeManagedObjectSource(objectClass.getName(),
					new HttpObjectManagedObjectSource<>(objectClass, acceptedContentTypes,
							this.singletonObjectParserList, defaultHttpObjectParser));
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
	public void setDefaultHttpObjectResponder(HttpObjectResponderServiceFactory objectResponderServiceFactory) {
		this.defaultHttpObjectResponderServiceFactory = objectResponderServiceFactory;
	}

	@Override
	public boolean isPathParameters(String path) {
		return this.routing.isPathParameters(path);
	}

	@Override
	public HttpUrlContinuation getHttpInput(boolean isSecure, String applicationPath) {
		HttpUrlContinuationImpl continuation = new HttpUrlContinuationImpl(isSecure, applicationPath);
		this.inputs.add(continuation);
		return continuation;
	}

	@Override
	public HttpInput getHttpInput(boolean isSecure, String httpMethodName, String applicationPath) {
		HttpMethod httpMethod = HttpMethod.getHttpMethod(httpMethodName);
		HttpInputImpl input = new HttpInputImpl(isSecure, httpMethod, applicationPath);
		this.inputs.add(input);
		return input;
	}

	@Override
	public void addHttpInputExplorer(HttpInputExplorer explorer) {
		this.httpInputExplorers.add(explorer);
	}

	@Override
	public void reroute(OfficeFlowSourceNode flowSourceNode) {
		this.officeArchitect.link(flowSourceNode, this.routingSection.getOfficeSectionInput(HANDLER_INPUT_NAME));
	}

	@Override
	public void intercept(OfficeFlowSinkNode flowSinkNode, OfficeFlowSourceNode flowSourceNode) {
		this.interceptors.add(new Interceptor(flowSinkNode, flowSourceNode));
	}

	@Override
	public void chainServicer(OfficeFlowSinkNode flowSinkNode, OfficeFlowSourceNode notHandledOutput) {
		this.chainedServicers.add(new ChainedServicer(flowSinkNode, notHandledOutput));
	}

	@Override
	public <H> AcceptNegotiatorBuilder<H> createAcceptNegotiator() {
		return new AcceptNegotiatorBuilderImpl<>();
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

		// Create the object response registrator
		ObjectResponseRegistrator objectResponseRegistrator = new ObjectResponseRegistrator();

		// Configure the object responder (if configured factories)
		if ((this.objectResponderFactories.size() > 0) || (this.defaultHttpObjectResponderServiceFactory != null)) {

			// Register the default object responder
			ObjectResponseManagedObjectSource objectResponseMos = objectResponseRegistrator.registerObjectResponse(200);

			// Use it to handle escalations
			this.routing.setHttpEscalationHandler(objectResponseMos);
		}

		// Load the intercept services
		NEXT_INTERCEPTOR: for (Class<?> interceptClass : this.officeSourceContext
				.loadOptionalServices(WebInterceptServiceFactory.class)) {

			// Ignore void intercept (allows dynamic determining if need to intercept)
			if (Void.TYPE.isAssignableFrom(interceptClass)) {
				continue NEXT_INTERCEPTOR;
			}

			// Obtain the intercept section name
			String sectionName = "_INTERCEPT_" + interceptClass.getName().replace('.', '_');

			// Load the type
			OfficeSectionType interceptSectionType = this.officeSourceContext.loadOfficeSectionType(sectionName,
					ClassSectionSource.class.getName(), interceptClass.getName(),
					this.officeSourceContext.createPropertyList());

			// Obtain the input name (ensuring only one input)
			String inputName = null;
			if (interceptSectionType.getOfficeSectionInputTypes().length == 1) {
				// Capture the input name
				inputName = interceptSectionType.getOfficeSectionInputTypes()[0].getOfficeSectionInputName();
			} else {
				// Provide error regarding the inputs
				StringBuilder message = new StringBuilder();
				message.append("Web intercept " + interceptClass.getName() + " must only have one input (inputs:");
				for (OfficeSectionInputType inputType : interceptSectionType.getOfficeSectionInputTypes()) {
					message.append(" " + inputType.getOfficeSectionInputName());
				}
				message.append(")");
				this.officeArchitect.addIssue(message.toString());
				continue NEXT_INTERCEPTOR; // can not load this intercepter
			}

			// Obtain the output name (ensuring only one output ignoring escalation only)
			String outputName = null;
			int outputCount = 0;
			StringBuilder message = new StringBuilder();
			NEXT_OUTPUT: for (OfficeSectionOutputType outputType : interceptSectionType.getOfficeSectionOutputTypes()) {

				// Ignore escalation only
				if (outputType.isEscalationOnly()) {
					continue NEXT_OUTPUT;
				}

				// Include output
				outputName = outputType.getOfficeSectionOutputName();
				outputCount++;
				message.append(" " + outputName);
			}
			if (outputCount != 1) {
				// Provide error regarding the outputs
				this.officeArchitect.addIssue("Web intercept " + interceptClass.getName()
						+ " must only have one output (outputs:" + message.toString() + ")");
				continue NEXT_INTERCEPTOR; // can not load this intercepter
			}

			// Add the intercept section
			OfficeSection interceptSection = this.officeArchitect.addOfficeSection(sectionName,
					ClassSectionSource.class.getName(), interceptClass.getName());

			// Register the intercept
			OfficeSectionInput interceptInput = interceptSection.getOfficeSectionInput(inputName);
			OfficeSectionOutput interceptOutput = interceptSection.getOfficeSectionOutput(outputName);
			this.interceptors.add(new Interceptor(interceptInput, interceptOutput));
		}

		// Determine if intercept
		if (this.interceptors.size() > 0) {

			// Obtain the interception
			Interception interception = routing.getInterception();

			// Obtain the section output
			OfficeFlowSourceNode interceptionOutput = this.routingSection
					.getOfficeSectionOutput(interception.getOutputName());
			for (Interceptor interceptor : this.interceptors) {

				// Link in interception
				this.officeArchitect.link(interceptionOutput, interceptor.flowSinkNode);

				// Set up for next iteration
				interceptionOutput = interceptor.flowSourceNode;
			}

			// Link interception back to routing
			OfficeSectionInput routingInput = this.routingSection.getOfficeSectionInput(interception.getInputName());
			this.officeArchitect.link(interceptionOutput, routingInput);
		}

		// Load in-line configured dependencies
		this.officeArchitect.addManagedFunctionAugmentor((context) -> {
			ManagedFunctionType<?, ?> functionType = context.getManagedFunctionType();
			for (ManagedFunctionObjectType<?> functionParameterType : functionType.getObjectTypes()) {
				Class<?> objectType = functionParameterType.getObjectType();

				// Determine if in-line configuration of dependency
				for (Object annotation : functionParameterType.getAnnotations()) {

					// Load the qualified object responses
					if (annotation instanceof HttpResponse) {
						HttpResponse httpResponse = (HttpResponse) annotation;
						objectResponseRegistrator.registerObjectResponse(httpResponse.status());
					}

					// Application object
					if (annotation instanceof HttpApplicationStateful) {
						HttpApplicationStateful stateful = (HttpApplicationStateful) annotation;
						this.addHttpApplicationObject(objectType, stateful.bind());
					}

					// Session object
					if (annotation instanceof HttpSessionStateful) {
						HttpSessionStateful stateful = (HttpSessionStateful) annotation;
						this.addHttpSessionObject(objectType, stateful.bind());
					}

					// HTTP parameters
					HttpParametersAnnotation httpParametersAnnotation = null;
					if (annotation instanceof HttpParameters) {
						httpParametersAnnotation = new HttpParametersAnnotation((HttpParameters) annotation);
					} else if (annotation instanceof HttpParametersAnnotation) {
						httpParametersAnnotation = (HttpParametersAnnotation) annotation;
					}
					if (httpParametersAnnotation != null) {
						this.addHttpRequestObject(objectType, true);
					}

					// HTTP object
					HttpObjectAnnotation httpObjectAnnotation = null;
					if (annotation instanceof HttpObject) {
						httpObjectAnnotation = new HttpObjectAnnotation((HttpObject) annotation);
					}
					if (annotation instanceof HttpObjectAnnotation) {
						httpObjectAnnotation = (HttpObjectAnnotation) annotation;
					}
					if (httpObjectAnnotation != null) {
						String[] acceptedContentTypes = httpObjectAnnotation.getAcceptedContentTypes();
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
							HttpPathParameterAnnotation.class, HttpValueLocation.PATH, objectType, context,
							(rawAnnotation) -> new HttpPathParameterAnnotation(rawAnnotation));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpQueryParameter.class,
							HttpQueryParameterAnnotation.class, HttpValueLocation.QUERY, objectType, context,
							(rawAnnotation) -> new HttpQueryParameterAnnotation(rawAnnotation));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpHeaderParameter.class,
							HttpHeaderParameterAnnotation.class, HttpValueLocation.HEADER, objectType, context,
							(rawAnnotation) -> new HttpHeaderParameterAnnotation(rawAnnotation));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpCookieParameter.class,
							HttpCookieParameterAnnotation.class, HttpValueLocation.COOKIE, objectType, context,
							(rawAnnotation) -> new HttpCookieParameterAnnotation(rawAnnotation));
					WebArchitectEmployer.this.loadInlineHttpArgument(annotation, HttpContentParameter.class,
							HttpContentParameterAnnotation.class, HttpValueLocation.ENTITY, objectType, context,
							(rawAnnotation) -> new HttpContentParameterAnnotation(rawAnnotation));
				}
			}
		});

		// Load explorers of HTTP inputs
		OfficeSectionInput handleInput = this.routingSection.getOfficeSectionInput(WebArchitect.HANDLER_INPUT_NAME);
		for (HttpInputExplorer explorer : this.httpInputExplorers) {
			handleInput.addExecutionExplorer((explore) -> {

				// Obtain the handling managed functions for each HTTP input
				ExecutionManagedFunction route = explore.getManagedFunction(
						Node.qualify(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
				Map<String, ExecutionManagedFunction> inputHandlers = new HashMap<>();
				for (ManagedFunctionFlowType<?> flowType : route.getManagedFunctionType().getFlowTypes()) {
					inputHandlers.put(flowType.getFlowName(), route.getManagedFunction(flowType));
				}

				// Allow exploring the input
				for (HttpInputImpl input : this.inputs) {

					// Obtain the input handler
					ExecutionManagedFunction inputHandler = inputHandlers.get(input.routeInput.getOutputName());
					if (input.routeInput.getHttpInputPath().isPathParameters()) {
						// Step over initialise path parameters function
						inputHandler = inputHandler.getNextManagedFunction();
					}

					// Allow exploring input
					final ExecutionManagedFunction finalInputHandler = inputHandler;
					final WebArchitectEmployer employer = this; // easy access
					explorer.explore(new HttpInputExplorerContext() {

						@Override
						public ExecutionManagedFunction getManagedFunction(String functionName) {
							return explore.getManagedFunction(functionName);
						}

						@Override
						public ExecutionManagedFunction getInitialManagedFunction() {
							return finalInputHandler;
						}

						@Override
						public boolean isSecure() {
							return input.isSecure;
						}

						@Override
						public HttpMethod getHttpMethod() {
							return input.httpMethod;
						}

						@Override
						public String getContextPath() {
							return employer.contextPath;
						}

						@Override
						public String getRoutePath() {
							return input.applicationPath;
						}

						@Override
						public String getApplicationPath() {
							return WebRouterBuilder.getContextQualifiedPath(WebArchitectEmployer.this.contextPath,
									input.applicationPath);
						}

						@Override
						public HttpObjectParserFactory[] getHttpObjectParserFactories() {
							if (employer.singletonObjectParserList.size() == 0) {
								return new HttpObjectParserFactory[] { employer.officeSourceContext
										.loadService(employer.defaultHttpObjectParserServiceFactory) };
							} else {
								return employer.singletonObjectParserList.toArray(
										new HttpObjectParserFactory[employer.singletonObjectParserList.size()]);
							}
						}

						@Override
						public HttpObjectResponderFactory[] getHttpObjectResponderFactories() {
							if (employer.objectResponderFactories.size() == 0) {
								return new HttpObjectResponderFactory[] { employer.officeSourceContext
										.loadService(employer.defaultHttpObjectResponderServiceFactory) };
							} else {
								return employer.objectResponderFactories.toArray(
										new HttpObjectResponderFactory[employer.objectResponderFactories.size()]);
							}
						}

						@Override
						public String getDocumentation() {
							return input.documentation;
						}
					});
				}
			});
		}

		// Chain in the servicer instances
		OfficeFlowSourceNode chainOutput = this.routingSection
				.getOfficeSectionOutput(HttpRouteSectionSource.UNHANDLED_OUTPUT_NAME);
		NEXT_CHAINED_SERVICER: for (ChainedServicer servicer : this.chainedServicers) {

			// Do nothing if no output (all handled by previous servicer)
			if (chainOutput == null) {
				continue NEXT_CHAINED_SERVICER;
			}

			// Link output to to input
			this.officeArchitect.link(chainOutput, servicer.flowSinkNode);

			// Set up for next chain
			chainOutput = servicer.notHandledOutput;
		}

		// Configure not handled
		if (chainOutput != null) {
			this.officeArchitect.link(chainOutput,
					this.routingSection.getOfficeSectionInput(HttpRouteSectionSource.NOT_FOUND_INPUT_NAME));
		}
	}

	/**
	 * Loads the in-line HTTP argument.
	 * 
	 * @param annotation         {@link Annotation}.
	 * @param annotationType     Type of {@link Annotation}.
	 * @param annotationWrapType Wrapping type for {@link Annotation}.
	 * @param valueLocation      {@link HttpValueLocation}.
	 * @param objectType         Parameter object type.
	 * @param context            {@link ManagedFunctionAugmentorContext}.
	 * @param wrapAnnotation     {@link Function} to wrap {@link Annotation} with
	 *                           wrapper.
	 * @return Indicates if loaded as argument.
	 */
	@SuppressWarnings("unchecked")
	private <P extends Annotation, W extends HttpParameterAnnotation> boolean loadInlineHttpArgument(Object annotation,
			Class<P> annotationType, Class<W> annotationWrapType, HttpValueLocation valueLocation, Class<?> objectType,
			ManagedFunctionAugmentorContext context, Function<P, W> wrapAnnotation) {

		// Determine if wrap type
		W annotationWrapper;
		if (annotationWrapType.isAssignableFrom(annotation.getClass())) {
			annotationWrapper = (W) annotation;

		} else if ((annotation instanceof Annotation)
				&& (annotationType.equals(((Annotation) annotation).annotationType()))) {
			annotationWrapper = wrapAnnotation.apply((P) annotation);

		} else {
			// Not particular HTTP parameter
			return false;
		}

		// Ensure parameter object is a String
		if (objectType != String.class) {
			this.officeArchitect.addIssue("Parameter must be " + String.class.getName() + " but was "
					+ objectType.getName() + " for function " + context.getManagedFunctionName());
		}

		// Add the HTTP argument
		String parameterName = annotationWrapper.getParameterName();
		String typeQualifier = annotationWrapper.getQualifier();
		this.addHttpArgument(parameterName, valueLocation).addTypeQualification(typeQualifier, String.class.getName());
		return true;
	}

	/**
	 * {@link HttpInput} implementation.
	 */
	private class HttpInputImpl implements HttpInput {

		/**
		 * Indicates if secure.
		 */
		protected final boolean isSecure;

		/**
		 * {@link HttpMethod}.
		 */
		private final HttpMethod httpMethod;

		/**
		 * Application path.
		 */
		protected final String applicationPath;

		/**
		 * {@link RouteInput}
		 */
		protected final RouteInput routeInput;

		/**
		 * {@link OfficeFlowSourceNode} to configure handling of this {@link HttpInput}.
		 */
		private final OfficeFlowSourceNode input;

		/**
		 * Documentation.
		 */
		private String documentation = null;

		/**
		 * Instantiate.
		 * 
		 * @param isSecure        Indicates if secure.
		 * @param httpMethod      {@link HttpMethod}.
		 * @param applicationPath Application path.
		 */
		private HttpInputImpl(boolean isSecure, HttpMethod httpMethod, String applicationPath) {
			this.isSecure = isSecure;
			this.httpMethod = httpMethod;
			this.applicationPath = applicationPath;
			this.routeInput = WebArchitectEmployer.this.routing.addRoute(isSecure, this.httpMethod,
					this.applicationPath);
			this.input = WebArchitectEmployer.this.routingSection
					.getOfficeSectionOutput(this.routeInput.getOutputName());
		}

		/*
		 * ================== HttpInput ====================
		 */

		@Override
		public HttpInputPath getPath() {
			return this.routeInput.getHttpInputPath();
		}

		@Override
		public OfficeFlowSourceNode getInput() {
			return this.input;
		}

		@Override
		public void setDocumentation(String documentation) {
			this.documentation = documentation;
		}
	}

	/**
	 * {@link HttpUrlContinuation} implementation.
	 */
	private class HttpUrlContinuationImpl extends HttpInputImpl implements HttpUrlContinuation {

		/**
		 * Mapping of parameter type to {@link OfficeFlowSinkNode} for redirects.
		 */
		private final Map<String, OfficeFlowSinkNode> redirects = new HashMap<>();

		/**
		 * Instantiate.
		 * 
		 * @param isSecure        Indicates if secure.
		 * @param applicationPath Application path.
		 */
		private HttpUrlContinuationImpl(boolean isSecure, String applicationPath) {
			super(isSecure, HttpMethod.GET, applicationPath);
		}

		/*
		 * =============== HttpUrlContinuation =============
		 */

		@Override
		public OfficeFlowSinkNode getRedirect(String parameterTypeName) {

			// Obtain the parameter type class
			Class<?> parameterType = CompileUtil.isBlank(parameterTypeName) ? Object.class
					: WebArchitectEmployer.this.officeSourceContext.loadClass(parameterTypeName);

			// Determine if already cached
			OfficeFlowSinkNode flowSinkNode = this.redirects.get(parameterTypeName);
			if (flowSinkNode != null) {
				return flowSinkNode;
			}

			// Not cached, so create
			try {
				// Create the redirect
				Redirect redirect = WebArchitectEmployer.this.routing.addRedirect(this.isSecure, this.routeInput,
						parameterType);

				// Obtain and cache the flow sink node for the redirect
				flowSinkNode = WebArchitectEmployer.this.routingSection.getOfficeSectionInput(redirect.getInputName());
				this.redirects.put(parameterTypeName, flowSinkNode);

				// Return the section input for redirect
				return flowSinkNode;

			} catch (Exception ex) {
				throw WebArchitectEmployer.this.officeArchitect.addIssue("Failed to create redirect to "
						+ this.applicationPath + (parameterTypeName == null ? " with null value type"
								: " with values type " + parameterTypeName),
						ex);
			}
		}
	}

	/**
	 * Creates the necessary {@link ObjectResponse} instances.
	 */
	private class ObjectResponseRegistrator {

		/**
		 * {@link DefaultHttpObjectResponder}.
		 */
		private DefaultHttpObjectResponder defaultHttpObjectResponder;

		/**
		 * Registered {@link ObjectResponseManagedObjectSource} instances by their
		 * {@link HttpStatus}.
		 */
		private Map<Integer, ObjectResponseManagedObjectSource> objectResponses = new HashMap<>();

		/**
		 * Obtains the {@link DefaultHttpObjectResponder}.
		 * 
		 * @return {@link DefaultHttpObjectResponder} or <code>null</code> if not
		 *         configured.
		 */
		private DefaultHttpObjectResponder getDefaultHttpObjectResponder() {

			// Easy access
			WebArchitectEmployer employer = WebArchitectEmployer.this;

			// Lazy load the default HTTP object responder
			if (this.defaultHttpObjectResponder == null) {
				this.defaultHttpObjectResponder = () -> employer.defaultHttpObjectResponderServiceFactory != null
						? employer.officeSourceContext.loadService(employer.defaultHttpObjectResponderServiceFactory)
						: null;

			}
			return this.defaultHttpObjectResponder;
		}

		/**
		 * Registers an {@link ObjectResponse} for injection.
		 * 
		 * @param statusCode {@link HttpStatus} for the {@link ObjectResponse}.
		 * @return Registered {@link ObjectResponseManagedObjectSource} for the
		 *         {@link HttpStatus}.
		 */
		private ObjectResponseManagedObjectSource registerObjectResponse(int statusCode) {

			// Easy access
			WebArchitectEmployer employer = WebArchitectEmployer.this;

			// Lazy register the object response
			ObjectResponseManagedObjectSource objectResponseMos = this.objectResponses.get(statusCode);
			if (objectResponseMos == null) {

				// Create the object response
				objectResponseMos = new ObjectResponseManagedObjectSource(HttpStatus.getHttpStatus(statusCode),
						employer.objectResponderFactories, this.getDefaultHttpObjectResponder());
				OfficeManagedObject managedObject = employer.officeArchitect
						.addOfficeManagedObjectSource("OBJECT_RESPONSE_" + statusCode, objectResponseMos)
						.addOfficeManagedObject("OBJECT_RESPONSE_" + statusCode, ManagedObjectScope.PROCESS);
				managedObject.addTypeQualification(String.valueOf(statusCode), ObjectResponse.class.getName());

				// OK status is default response
				if (statusCode == HttpStatus.OK.getStatusCode()) {
					managedObject.addTypeQualification(null, ObjectResponse.class.getName());
				}

				// Register the object response
				this.objectResponses.put(statusCode, objectResponseMos);
			}

			// Return the object response
			return objectResponseMos;
		}
	}

	/**
	 * Intercepts the {@link HttpRequest} before web application functionality.
	 */
	private static class Interceptor {

		/**
		 * {@link OfficeFlowSinkNode}.
		 */
		public final OfficeFlowSinkNode flowSinkNode;

		/**
		 * {@link OfficeFlowSourceNode}.
		 */
		public final OfficeFlowSourceNode flowSourceNode;

		/**
		 * Initiate.
		 * 
		 * @param flowSinkNode   {@link OfficeFlowSinkNode}.
		 * @param flowSourceNode {@link OfficeFlowSourceNode}.
		 */
		private Interceptor(OfficeFlowSinkNode flowSinkNode, OfficeFlowSourceNode flowSourceNode) {
			this.flowSinkNode = flowSinkNode;
			this.flowSourceNode = flowSourceNode;
		}
	}

	/**
	 * Chained servicer.
	 */
	private static class ChainedServicer {

		/**
		 * {@link OfficeFlowSinkNode}.
		 */
		public final OfficeFlowSinkNode flowSinkNode;

		/**
		 * {@link OfficeFlowSourceNode}. May be <code>null</code>.
		 */
		public final OfficeFlowSourceNode notHandledOutput;

		/**
		 * Initiate.
		 * 
		 * @param flowSinkNode     {@link OfficeFlowSinkNode}.
		 * @param notHandledOutput {@link OfficeFlowSourceNode}. May be
		 *                         <code>null</code>.
		 */
		private ChainedServicer(OfficeFlowSinkNode flowSinkNode, OfficeFlowSourceNode notHandledOutput) {
			this.flowSinkNode = flowSinkNode;
			this.notHandledOutput = notHandledOutput;
		}
	}

}
