/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.classpath.ClasspathResourceSystemFactory;
import net.officefloor.web.resource.file.FileResourceSystemService;
import net.officefloor.web.resource.impl.HttpResourceStoreImpl;
import net.officefloor.web.resource.source.AbstractSendHttpFileFunction;
import net.officefloor.web.resource.source.AbstractSendHttpFileFunction.Dependencies;
import net.officefloor.web.resource.source.AbstractSendHttpFileFunction.Flows;
import net.officefloor.web.resource.source.HttpPath;
import net.officefloor.web.resource.source.HttpResourceCacheManagedObjectSource;
import net.officefloor.web.resource.source.HttpResourceStoreManagedObjectSource;
import net.officefloor.web.resource.source.SendCachedHttpFileFunction;
import net.officefloor.web.resource.source.SendStoreHttpFileFunction;
import net.officefloor.web.resource.source.ServiceHttpRequestFunction;
import net.officefloor.web.resource.source.TranslateHttpPathToWebServicerFunction;
import net.officefloor.web.resource.source.TriggerSendHttpFileFunction;
import net.officefloor.web.resource.spi.FileCacheFactory;
import net.officefloor.web.resource.spi.FileCacheService;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceSystemService;
import net.officefloor.web.resource.spi.ResourceTransformer;
import net.officefloor.web.resource.spi.ResourceTransformerFactory;
import net.officefloor.web.resource.spi.ResourceTransformerService;
import net.officefloor.web.route.WebRouter;
import net.officefloor.web.route.WebServicer;
import net.officefloor.web.security.build.AbstractHttpSecurable;
import net.officefloor.web.security.build.HttpSecurableBuilder;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.section.HttpFlowSecurer;

/**
 * Employs a {@link HttpResourceArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResourceArchitectEmployer implements HttpResourceArchitect {

	/**
	 * Employs the {@link HttpResourceArchitect}.
	 * 
	 * @param webArchitect        {@link WebArchitect}.
	 * @param securityArchitect   {@link HttpSecurityArchitect}.
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext}.
	 * @return {@link HttpResourceArchitect}.
	 */
	public static HttpResourceArchitect employHttpResourceArchitect(WebArchitect webArchitect,
			HttpSecurityArchitect securityArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		return new HttpResourceArchitectEmployer(webArchitect, securityArchitect, officeArchitect, officeSourceContext);
	}

	/**
	 * {@link WebArchitect}.
	 */
	private final WebArchitect webArchitect;

	/**
	 * {@link HttpSecurityArchitect}.
	 */
	private final HttpSecurityArchitect securityArchitect;

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext officeSourceContext;

	/**
	 * {@link OfficeSection} for the {@link HttpResourceSectionSource}.
	 */
	private final OfficeSection section;

	/**
	 * {@link HttpResourceSource} instances.
	 */
	private final List<HttpResourceSource> httpResourceSources = new LinkedList<>();

	/**
	 * {@link Map} of {@link HttpResource} path to its {@link OfficeFlowSinkNode}.
	 */
	private final Map<String, OfficeFlowSinkNode> linkedResources = new HashMap<>();

	/**
	 * Next {@link HttpResourceSource} index (to ensure unique names).
	 */
	private int nextResourceSourceIndex = 1;

	/**
	 * Indicates whether to include the default {@link HttpResourceStore}.
	 */
	private boolean isIncludeDefaultHttpResource = true;

	/**
	 * Instantiate.
	 * 
	 * @param webArchitect        {@link WebArchitect}.
	 * @param securityArchitect   {@link HttpSecurityArchitect}.
	 * @param officeArchitect     {@link OfficeArchitect}.
	 * @param officeSourceContext {@link OfficeSourceContext}.
	 */
	private HttpResourceArchitectEmployer(WebArchitect webArchitect, HttpSecurityArchitect securityArchitect,
			OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		this.webArchitect = webArchitect;
		this.securityArchitect = securityArchitect;
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;

		// Add the resource service section
		HttpResourceSectionSource sectionSource = new HttpResourceSectionSource();
		this.section = this.officeArchitect.addOfficeSection("_resources_", sectionSource, null);
	}

	/*
	 * =================== HttpResourceArchitect ========================
	 */

	@Override
	public OfficeFlowSinkNode getResource(String resourcePath) {

		// Ensure prefix with /
		if (CompileUtil.isBlank(resourcePath)) {
			resourcePath = "/";
		} else if (!resourcePath.startsWith("/")) {
			resourcePath = "/" + resourcePath;
		}

		// Obtain and cache resource
		OfficeFlowSinkNode resource = this.linkedResources.get(resourcePath);
		if (resource == null) {
			resource = section.getOfficeSectionInput("send_" + resourcePath);
			this.linkedResources.put(resourcePath, resource);
		}
		return resource;
	}

	@Override
	public HttpResourcesBuilder addHttpResources(ResourceSystemFactory resourceSystemService, String location) {
		HttpResourceSource source = new HttpResourceSource(resourceSystemService, location);
		this.httpResourceSources.add(source);
		return source;
	}

	@Override
	public HttpResourcesBuilder addHttpResources(String protocolLocation) {

		// Obtain the resource system factory
		Function<String, ResourceSystemFactory> getResourceSystemFactory = (protocol) -> {
			for (ResourceSystemFactory factory : this.officeSourceContext.loadServices(ResourceSystemService.class,
					null)) {
				if (protocol.equalsIgnoreCase(factory.getProtocolName())) {
					// Found resource system for protocol
					return factory;
				}
			}
			return null; // not found
		};

		// Obtain the protocol and location
		int splitIndex = protocolLocation.indexOf(':');
		if (splitIndex < 0) {
			// No protocol specified, so consider file
			return this.addHttpResources(new FileResourceSystemService(), protocolLocation);
		}
		String protocol = protocolLocation.substring(0, splitIndex);
		String location = protocolLocation.substring(splitIndex + 1);

		// Obtain the resource system factory
		ResourceSystemFactory factory = getResourceSystemFactory.apply(protocol);
		if (factory != null) {
			return this.addHttpResources(factory, location);
		}

		// Determine if file (windows may have C:\dev)
		File resourceFile = new File(protocolLocation);
		if (resourceFile.exists()) {
			// Default file location
			return this.addHttpResources(new FileResourceSystemService(), protocolLocation);
		}

		// As here, protocol not available
		throw this.officeArchitect.addIssue("Resource '" + protocol + "' not available.  Please ensure its "
				+ ResourceSystemService.class.getSimpleName()
				+ " implementation is on the class path and configured as a service.");
	}

	@Override
	public void disableDefaultHttpResources() {
		this.isIncludeDefaultHttpResource = false;
	}

	@Override
	public void informWebArchitect() throws IOException {

		// Determine if include default HTTP resources
		if (this.isIncludeDefaultHttpResource) {
			HttpResourceSource defaultResourceSource = new HttpResourceSource(
					new ClasspathResourceSystemFactory(this.officeSourceContext.getClassLoader()), "PUBLIC");
			this.httpResourceSources.add(defaultResourceSource);
		}

		// Obtain the file cache factory
		FileCacheFactory fileCacheFactory = this.officeSourceContext.loadService(FileCacheService.class,
				new TemporaryDirectoryFileCacheService());

		// Load the resource sources
		int nextStoreIndex = 1;
		List<HttpResourceStore> stores = new ArrayList<>(this.httpResourceSources.size());
		for (HttpResourceSource httpResourceSource : this.httpResourceSources) {

			// Build the resource store
			ResourceTransformer[] resourceTransformers = httpResourceSource.resourceTransformers
					.toArray(new ResourceTransformer[httpResourceSource.resourceTransformers.size()]);
			HttpResourceStoreImpl store = new HttpResourceStoreImpl(httpResourceSource.location,
					httpResourceSource.resourceSystemService, fileCacheFactory, resourceTransformers,
					httpResourceSource.directoryDefaultResourceNames);
			stores.add(store);

			// Register the managed objects (for auto-wiring)
			String resourceNameSuffix = "_" + String.valueOf(nextStoreIndex++) + "_" + httpResourceSource.location;
			OfficeManagedObject cacheManagedObject = this.officeArchitect
					.addOfficeManagedObjectSource(HttpResourceCache.class.getSimpleName() + resourceNameSuffix,
							new HttpResourceCacheManagedObjectSource(store.getCache()))
					.addOfficeManagedObject(HttpResourceCache.class.getSimpleName() + resourceNameSuffix,
							ManagedObjectScope.PROCESS);
			OfficeManagedObject storeManagedObject = this.officeArchitect
					.addOfficeManagedObjectSource(HttpResourceStore.class.getSimpleName() + resourceNameSuffix,
							new HttpResourceStoreManagedObjectSource(store))
					.addOfficeManagedObject(HttpResourceStore.class.getSimpleName() + resourceNameSuffix,
							ManagedObjectScope.PROCESS);

			// Add any type qualifications
			for (String typeQualification : httpResourceSource.typeQualifications) {
				cacheManagedObject.addTypeQualification(typeQualification, HttpResourceCache.class.getName());
				storeManagedObject.addTypeQualification(typeQualification, HttpResourceStore.class.getName());
			}

			// Link managed objects to section
			this.officeArchitect.link(
					this.section.getOfficeSectionObject(
							HttpResourceCache.class.getSimpleName() + httpResourceSource.nameSuffix),
					cacheManagedObject);
			this.officeArchitect.link(
					this.section.getOfficeSectionObject(
							HttpResourceStore.class.getSimpleName() + httpResourceSource.nameSuffix),
					storeManagedObject);
		}

		// Ensure linked resources are available
		for (String resourcePath : this.linkedResources.keySet()) {

			// Ensure the resource is available from one of the stores
			boolean isAvailable = false;
			FOUND_RESOURCE: for (HttpResourceStore store : stores) {
				HttpResource resource = store.getHttpResource(resourcePath);
				if (resource.isExist() && (resource instanceof HttpFile)) {
					isAvailable = true;
					break FOUND_RESOURCE;
				}
			}
			if (!isAvailable) {
				throw this.officeArchitect.addIssue("Can not find HTTP resource '" + resourcePath + "'");
			}
		}

		// Configure to service resources after the application
		this.webArchitect.chainServicer(this.section.getOfficeSectionInput(HttpResourceSectionSource.INPUT_NAME),
				this.section.getOfficeSectionOutput(HttpResourceSectionSource.NOT_FOUND_OUTPUT_NAME));
	}

	/**
	 * Source of {@link HttpResource} instances.
	 */
	private class HttpResourceSource extends AbstractHttpSecurable implements HttpResourcesBuilder {

		/**
		 * {@link ResourceSystemFactory}.
		 */
		private final ResourceSystemFactory resourceSystemService;

		/**
		 * Location.
		 */
		private final String location;

		/**
		 * {@link ResourceTransformer} instances.
		 */
		private final List<ResourceTransformer> resourceTransformers = new LinkedList<>();

		/**
		 * Type qualifiers.
		 */
		private final List<String> typeQualifications = new LinkedList<>();

		/**
		 * Context path of resources within the application.
		 */
		private String contextPath = "/";

		/**
		 * Directory default resource names.
		 */
		private String[] directoryDefaultResourceNames = new String[] { "index.html" };

		/**
		 * Name suffix to use in configuring this {@link HttpResourceSource}.
		 */
		private final String nameSuffix;

		/**
		 * {@link AbstractHttpSecurable}.
		 */
		private AbstractHttpSecurable securable = null;

		/**
		 * Instantiate
		 * 
		 * @param resourceSystemService {@link ResourceSystemFactory}.
		 * @param location              Location.
		 */
		private HttpResourceSource(ResourceSystemFactory resourceSystemService, String location) {
			this.resourceSystemService = resourceSystemService;
			this.location = location;

			// Provide name suffix to identify this resource source
			this.nameSuffix = "_" + String.valueOf(HttpResourceArchitectEmployer.this.nextResourceSourceIndex++) + "_"
					+ this.location;
		}

		/*
		 * ================ HttpResourcesBuilder =====================
		 */

		@Override
		public void setContextPath(String contextPath) {
			this.contextPath = contextPath == null ? "/"
					: WebRouter.transformToCanonicalPath(contextPath.startsWith("/") ? contextPath : "/" + contextPath);
		}

		@Override
		public void addTypeQualifier(String qualifier) {
			this.typeQualifications.add(qualifier);
		}

		@Override
		public void addResourceTransformer(ResourceTransformer transformer) {
			this.resourceTransformers.add(transformer);
		}

		@Override
		public void addResourceTransformer(String name) {

			// Search for appropriate resource transformer
			for (ResourceTransformerFactory transformerFactory : HttpResourceArchitectEmployer.this.officeSourceContext
					.loadServices(ResourceTransformerService.class, null)) {
				if (name.equalsIgnoreCase(transformerFactory.getName())) {

					// Create and add the resource transformer
					ResourceTransformer transformer = transformerFactory.createResourceTransformer();
					this.addResourceTransformer(transformer);
					return;
				}
			}

			// As here, resource transformer not available
			throw HttpResourceArchitectEmployer.this.officeArchitect.addIssue("Resource transformer '" + name
					+ "' not available.  Please ensure its " + ResourceTransformerFactory.class.getSimpleName()
					+ " implementation is on the class path and configured as a service.");
		}

		@Override
		public void setDirectoryDefaultResourceNames(String... defaultResourceNames) {
			this.directoryDefaultResourceNames = defaultResourceNames;
		}

		@Override
		public HttpSecurableBuilder getHttpSecurer() {
			if (this.securable == null) {
				this.securable = new AbstractHttpSecurable() {
				};
			}
			return this.securable;
		}
	}

	/**
	 * {@link SectionSource} to service the {@link HttpResource}.
	 */
	@PrivateSource
	private class HttpResourceSectionSource extends AbstractSectionSource {

		/**
		 * {@link SectionInput} name.
		 */
		private static final String INPUT_NAME = "service";

		/**
		 * Name of {@link SectionOutput} if {@link HttpResource} not found.
		 */
		private static final String NOT_FOUND_OUTPUT_NAME = "NotFound";

		/*
		 * ================== SectionSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

			// Create the common dependencies
			SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
					ServerHttpConnection.class.getName());

			// Configure not found section output
			SectionOutput notFoundOutput = designer.addSectionOutput(NOT_FOUND_OUTPUT_NAME, WebServicer.class.getName(),
					false);

			// Build translate to HTTP path to Web Servicer
			final String translateFunctionName = "_translate_";
			SectionFunctionNamespace translateNamespace = designer.addSectionFunctionNamespace(translateFunctionName,
					new TranslateHttpPathToWebServicerFunctionSource());
			SectionFunction translateFunction = translateNamespace.addSectionFunction(translateFunctionName,
					TranslateHttpPathToWebServicerFunctionSource.FUNCTION_NAME);
			translateFunction.getFunctionObject(TranslateHttpPathToWebServicerFunction.Dependencies.HTTP_PATH.name())
					.flagAsParameter();
			designer.link(translateFunction, notFoundOutput);

			// Build store functions (in reverse order to get to chain input)
			SectionFlowSinkNode nextHandler = translateFunction;
			for (int i = 0; i < HttpResourceArchitectEmployer.this.httpResourceSources.size(); i++) {
				HttpResourceSource source = HttpResourceArchitectEmployer.this.httpResourceSources.get(i);

				// Add the store function
				String functionName = "store" + source.nameSuffix;
				SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName,
						new SendHttpFileManagedFunctionSource<>(new SendStoreHttpFileFunction(source.contextPath),
								HttpResourceStore.class));
				SectionFunction function = namespace.addSectionFunction(functionName,
						SendHttpFileManagedFunctionSource.FUNCTION_NAME);

				// Configure the dependencies
				function.getFunctionObject(Dependencies.HTTP_PATH.name()).flagAsParameter();
				designer.link(function.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()),
						serverHttpConnection);

				// Configure dependency to resources store
				SectionObject httpResourceStore = designer.addSectionObject(
						HttpResourceStore.class.getSimpleName() + source.nameSuffix, HttpResourceStore.class.getName());
				designer.link(function.getFunctionObject(Dependencies.HTTP_RESOURCES.name()), httpResourceStore);

				// Configure as handling not available
				FunctionFlow notFoundFunctionFlow = function.getFunctionFlow(Flows.NOT_AVAILABLE.name());
				designer.link(notFoundFunctionFlow, nextHandler, false);

				// Determine if secure
				if (source.securable == null) {
					// Not secure, so function is next handler
					nextHandler = function;

				} else {
					// Secure the access to the resources
					HttpFlowSecurer securer = HttpResourceArchitectEmployer.this.securityArchitect
							.createHttpSecurer(source.securable).createFlowSecurer();
					nextHandler = securer.secureFlow(designer, HttpPath.class, function, nextHandler);
				}
			}

			// Build cache functions (in reverse order to get to chain input)
			for (int i = 0; i < HttpResourceArchitectEmployer.this.httpResourceSources.size(); i++) {
				HttpResourceSource source = HttpResourceArchitectEmployer.this.httpResourceSources.get(i);

				// Add the cache function
				String functionName = "cache" + source.nameSuffix;
				SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName,
						new SendHttpFileManagedFunctionSource<>(new SendCachedHttpFileFunction(source.contextPath),
								HttpResourceCache.class));
				SectionFunction function = namespace.addSectionFunction(functionName,
						SendHttpFileManagedFunctionSource.FUNCTION_NAME);

				// Configure the dependencies
				function.getFunctionObject(Dependencies.HTTP_PATH.name()).flagAsParameter();
				designer.link(function.getFunctionObject(Dependencies.SERVER_HTTP_CONNECTION.name()),
						serverHttpConnection);

				// Configure dependency to resource cache
				SectionObject httpResourceCache = designer.addSectionObject(
						HttpResourceCache.class.getSimpleName() + source.nameSuffix, HttpResourceCache.class.getName());
				designer.link(function.getFunctionObject(Dependencies.HTTP_RESOURCES.name()), httpResourceCache);

				// Configure as handling not available
				FunctionFlow notFoundFunctionFlow = function.getFunctionFlow(Flows.NOT_AVAILABLE.name());
				designer.link(notFoundFunctionFlow, nextHandler, false);

				// Determine if secure
				if (source.securable == null) {
					// Not secure, so function is next handler
					nextHandler = function;

				} else {
					// Secure the access to the resources
					HttpFlowSecurer securer = HttpResourceArchitectEmployer.this.securityArchitect
							.createHttpSecurer(source.securable).createFlowSecurer();
					nextHandler = securer.secureFlow(designer, HttpPath.class, function, nextHandler);
				}
			}

			// Create the section input to service requests
			SectionInput input = designer.addSectionInput(INPUT_NAME, null);

			// Create the service HTTP request trigger function
			SectionFunctionNamespace serviceNamespace = designer.addSectionFunctionNamespace("service",
					new ServiceHttpRequestManagedFunctionSource());
			SectionFunction serviceFunction = serviceNamespace.addSectionFunction("service",
					ServiceHttpRequestManagedFunctionSource.FUNCTION_NAME);
			serviceFunction.getFunctionObject(ServiceHttpRequestFunction.Dependencies.WEB_SERVICER.name())
					.flagAsParameter();
			designer.link(
					serviceFunction
							.getFunctionObject(ServiceHttpRequestFunction.Dependencies.SERVER_HTTP_CONNECTION.name()),
					serverHttpConnection);
			designer.link(input, serviceFunction);
			designer.link(serviceFunction, nextHandler);

			// Configure triggering the resource
			Set<String> configuredResourcePaths = new HashSet<>();
			for (String resourcePath : HttpResourceArchitectEmployer.this.linkedResources.keySet()) {

				// Determine if already configured resource path
				if (configuredResourcePaths.contains(resourcePath)) {
					return; // already configured
				}

				// Add the trigger function
				String functionName = "trigger_" + resourcePath;
				SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName,
						new TriggerSendHttpFileManagedFunctionSource(resourcePath));
				SectionFunction function = namespace.addSectionFunction(functionName,
						TriggerSendHttpFileManagedFunctionSource.FUNCTION_NAME);

				// Configure triggering send
				designer.link(function, nextHandler);

				// Configure handling trigger
				SectionInput send = designer.addSectionInput("send_" + resourcePath, null);
				designer.link(send, function);
			}
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link ServiceHttpRequestFunction}.
	 */
	@PrivateSource
	private static class ServiceHttpRequestManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private static final String FUNCTION_NAME = "function";

		/*
		 * ================ ManagedFunctionSource ================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the function
			ManagedFunctionTypeBuilder<ServiceHttpRequestFunction.Dependencies, None> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, ServiceHttpRequestFunction.Dependencies.class, None.class)
					.setFunctionFactory(new ServiceHttpRequestFunction());
			function.addObject(ServerHttpConnection.class)
					.setKey(ServiceHttpRequestFunction.Dependencies.SERVER_HTTP_CONNECTION);
			function.addObject(WebServicer.class).setKey(ServiceHttpRequestFunction.Dependencies.WEB_SERVICER);
			function.setReturnType(HttpPath.class);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link AbstractSendHttpFileFunction}.
	 */
	@PrivateSource
	private static class SendHttpFileManagedFunctionSource<R> extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private static final String FUNCTION_NAME = "function";

		/**
		 * {@link AbstractSendHttpFileFunction}.
		 */
		private final AbstractSendHttpFileFunction<R> sendHttpFileFunction;

		/**
		 * Resources type.
		 */
		private final Class<R> resourcesType;

		/**
		 * Instantiate.
		 * 
		 * @param sendHttpFileFunction {@link AbstractSendHttpFileFunction}.
		 */
		private SendHttpFileManagedFunctionSource(AbstractSendHttpFileFunction<R> sendHttpFileFunction,
				Class<R> resourcesType) {
			this.sendHttpFileFunction = sendHttpFileFunction;
			this.resourcesType = resourcesType;
		}

		/*
		 * ================ ManagedFunctionSource ================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the function
			ManagedFunctionTypeBuilder<Dependencies, Flows> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, Dependencies.class, Flows.class)
					.setFunctionFactory(this.sendHttpFileFunction);
			function.addObject(HttpPath.class).setKey(Dependencies.HTTP_PATH);
			function.addObject(this.resourcesType).setKey(Dependencies.HTTP_RESOURCES);
			function.addObject(ServerHttpConnection.class).setKey(Dependencies.SERVER_HTTP_CONNECTION);
			ManagedFunctionFlowTypeBuilder<Flows> flow = function.addFlow();
			flow.setKey(Flows.NOT_AVAILABLE);
			flow.setArgumentType(HttpPath.class);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link TriggerSendHttpFileFunction}.
	 */
	@PrivateSource
	private static class TriggerSendHttpFileManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private static final String FUNCTION_NAME = "function";

		/**
		 * Path.
		 */
		private final String path;

		/**
		 * Instantiate.
		 * 
		 * @param path Path.
		 */
		public TriggerSendHttpFileManagedFunctionSource(String path) {
			this.path = path;
		}

		/*
		 * =============== ManagedFunctionSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the function
			ManagedFunctionTypeBuilder<None, None> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, None.class, None.class)
					.setFunctionFactory(new TriggerSendHttpFileFunction(path));
			function.setReturnType(HttpPath.class);
		}
	}

	/**
	 * {@link ManagedFunctionSource} to translate the {@link HttpPath} to
	 * {@link WebServicer}.
	 */
	@PrivateSource
	private static class TranslateHttpPathToWebServicerFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private static final String FUNCTION_NAME = "function";

		/*
		 * =============== ManagedFunctionSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the function
			ManagedFunctionTypeBuilder<TranslateHttpPathToWebServicerFunction.Dependencies, None> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, TranslateHttpPathToWebServicerFunction.Dependencies.class,
							None.class)
					.setFunctionFactory(new TranslateHttpPathToWebServicerFunction());
			function.addObject(HttpPath.class).setKey(TranslateHttpPathToWebServicerFunction.Dependencies.HTTP_PATH);
			function.setReturnType(WebServicer.class);
		}
	}

}
