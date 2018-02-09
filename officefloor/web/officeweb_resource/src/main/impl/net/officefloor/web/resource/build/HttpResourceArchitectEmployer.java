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
package net.officefloor.web.resource.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
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
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.classpath.ClasspathResourceSystemService;
import net.officefloor.web.resource.file.FileResourceSystemService;
import net.officefloor.web.resource.impl.HttpResourceStoreImpl;
import net.officefloor.web.resource.source.HttpPath;
import net.officefloor.web.resource.source.HttpResourceCacheManagedObjectSource;
import net.officefloor.web.resource.source.HttpResourceStoreManagedObjectSource;
import net.officefloor.web.resource.source.SendCachedHttpFileFunction;
import net.officefloor.web.resource.source.SendCachedHttpFileFunction.Dependencies;
import net.officefloor.web.resource.source.SendHttpFileFunction;
import net.officefloor.web.resource.source.ServiceHttpRequestFunction;
import net.officefloor.web.resource.source.TriggerSendHttpFileFunction;
import net.officefloor.web.resource.spi.FileCacheFactory;
import net.officefloor.web.resource.spi.FileCacheService;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceSystemService;
import net.officefloor.web.resource.spi.ResourceTransformer;
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
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param securityArchitect
	 *            {@link HttpSecurityArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
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
	 * {@link HttpResourceSource} instances.
	 */
	private final List<HttpResourceSource> httpResourceSources = new LinkedList<>();

	/**
	 * {@link ResourceLink} instances.
	 */
	private final List<ResourceLink> resourceLinks = new LinkedList<>();

	/**
	 * {@link EscalationResource} instances.
	 */
	private final List<EscalationResource> escalationResources = new LinkedList<>();

	/**
	 * Next {@link HttpResourceSource} index (to ensure unique names).
	 */
	private int nextResourceSourceIndex = 1;

	/**
	 * Instantiate.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param securityArchitect
	 *            {@link HttpSecurityArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 */
	private HttpResourceArchitectEmployer(WebArchitect webArchitect, HttpSecurityArchitect securityArchitect,
			OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		this.webArchitect = webArchitect;
		this.securityArchitect = securityArchitect;
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;
	}

	/*
	 * =================== HttpResourceArchitect ========================
	 */

	@Override
	public void link(OfficeSectionOutput output, String resourcePath) {
		this.resourceLinks.add(new ResourceLink(output, resourcePath));
	}

	@Override
	public void link(OfficeEscalation escalation, String resourcePath) {
		this.escalationResources.add(new EscalationResource(escalation, resourcePath));
	}

	@Override
	public HttpResourcesBuilder addHttpResources(ResourceSystemFactory resourceSystemService, String location) {
		HttpResourceSource source = new HttpResourceSource(resourceSystemService, location);
		this.httpResourceSources.add(source);
		return source;
	}

	@Override
	public HttpResourcesBuilder addHttpResources(String protocolLocation) {

		// Obtain the protocol and location
		int splitIndex = protocolLocation.indexOf(':');
		if (splitIndex < 0) {
			// No protocol specified, so consider file
			return this.addHttpResources(new FileResourceSystemService(), protocolLocation);
		}
		String protocol = protocolLocation.substring(0, splitIndex);
		String location = protocolLocation.substring(splitIndex + 1);

		// Obtain the resource system factory
		for (ResourceSystemFactory factory : this.officeSourceContext.loadServices(ResourceSystemService.class, null)) {
			if (protocol.equalsIgnoreCase(factory.getProtocolName())) {
				// Found resource system for protocol
				return this.addHttpResources(factory, location);
			}
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void informWebArchitect() throws IOException {

		// Ensure at least one resource source
		if (this.httpResourceSources.size() == 0) {
			// None configured, so use default class path
			HttpResourceSource defaultResourceSource = new HttpResourceSource(new ClasspathResourceSystemService(),
					"PUBLIC");
			this.httpResourceSources.add(defaultResourceSource);
		}

		// Obtain the file cache factory
		FileCacheFactory fileCacheFactory = this.officeSourceContext.loadService(FileCacheService.class,
				new TemporaryDirectoryFileCacheService());

		// Add the resource service section
		HttpResourceSectionSource sectionSource = new HttpResourceSectionSource();
		OfficeSection section = this.officeArchitect.addOfficeSection("_resources_", sectionSource, null);

		// Load the resource sources
		int nextStoreIndex = 1;
		List<HttpResourceStore> stores = new ArrayList<>(this.httpResourceSources.size());
		for (HttpResourceSource httpResourceSource : this.httpResourceSources) {

			// Build the resource store
			ResourceTransformer[] resourceTransformers = httpResourceSource.resourceTransformers
					.toArray(new ResourceTransformer[httpResourceSource.resourceTransformers.size()]);
			HttpResourceStoreImpl store = new HttpResourceStoreImpl(httpResourceSource.location,
					httpResourceSource.resourceSystemService, httpResourceSource.contextPath, fileCacheFactory,
					resourceTransformers, httpResourceSource.directoryDefaultResourceNames);
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

			// Link managed objects to section
			this.officeArchitect.link(
					section.getOfficeSectionObject(
							HttpResourceCache.class.getSimpleName() + httpResourceSource.nameSuffix),
					cacheManagedObject);
			this.officeArchitect.link(
					section.getOfficeSectionObject(
							HttpResourceStore.class.getSimpleName() + httpResourceSource.nameSuffix),
					storeManagedObject);
		}

		// Link to resources
		Map<String, OfficeSectionInput> resourceSendTriggers = new HashMap<>();
		for (ResourceLink link : this.resourceLinks) {
			OfficeSectionInput trigger = this.getResourceSender(link.resourcePath, section, stores,
					resourceSendTriggers);
			this.officeArchitect.link(link.sectionOutput, trigger);
		}
		for (EscalationResource escalation : this.escalationResources) {
			OfficeSectionInput trigger = this.getResourceSender(escalation.resourcePath, section, stores,
					resourceSendTriggers);
			this.officeArchitect.link(escalation.escalation, trigger);
		}

		// Configure to service resources after the application
		this.webArchitect.chainServicer(section.getOfficeSectionInput(HttpResourceSectionSource.INPUT_NAME),
				section.getOfficeSectionOutput(HttpResourceSectionSource.NOT_FOUND_OUTPUT_NAME));
	}

	/**
	 * Obtains the {@link OfficeSectionInput} to handle sending the resource.
	 * 
	 * @param resourcePath
	 *            Path to the resource.
	 * @param section
	 *            {@link OfficeSection} for handling sending resources.
	 * @param stores
	 *            Listing of {@link HttpResourceStore} instances to validate
	 *            resource exists.
	 * @param resourceSendTriggers
	 *            {@link Map} of resource path to handling
	 *            {@link OfficeSectionInput}.
	 * @return {@link OfficeSectionInput} to handle sending the resource.
	 * @throws IOException
	 *             If fails to find the resource on checking exists.
	 */
	private OfficeSectionInput getResourceSender(String resourcePath, OfficeSection section,
			List<HttpResourceStore> stores, Map<String, OfficeSectionInput> resourceSendTriggers) throws IOException {

		// Obtain the send trigger
		OfficeSectionInput trigger = resourceSendTriggers.get(resourcePath);
		if (trigger == null) {
			// Register the trigger
			trigger = section.getOfficeSectionInput("send_" + resourcePath);
			resourceSendTriggers.put(resourcePath, trigger);
		}

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

		// Return the trigger
		return trigger;
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
		 * Context path of resources within the application.
		 */
		private String contextPath = null;

		/**
		 * {@link ResourceTransformer} instances.
		 */
		private final List<ResourceTransformer> resourceTransformers = new LinkedList<>();

		/**
		 * Directory default resource names.
		 */
		private final String[] directoryDefaultResourceNames = new String[] { "index.html" };

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
		 * @param resourceSystemService
		 *            {@link ResourceSystemFactory}.
		 * @param location
		 *            Location.
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
			// TODO Auto-generated method stub

		}

		@Override
		public void addTypeQualifier(String qualifier) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addResourceTransformer(ResourceTransformer transformer) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addResourceTransformer(String name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setDirectoryDefaultResourceNames(String... defaultResourceNames) {
			// TODO Auto-generated method stub

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
			this.resourcePath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
		}
	}

	/**
	 * Resource to handle {@link Escalation}.
	 */
	private static class EscalationResource {

		/**
		 * {@link OfficeEscalation}.
		 */
		public final OfficeEscalation escalation;

		/**
		 * Resource path.
		 */
		public final String resourcePath;

		/**
		 * Initiate.
		 * 
		 * @param escalation
		 *            {@link OfficeEscalation}.
		 * @param resourcePath
		 *            Resource path.
		 */
		public EscalationResource(OfficeEscalation escalation, String resourcePath) {
			this.escalation = escalation;
			this.resourcePath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
		}
	}

	/**
	 * {@link SectionSource} to service the {@link HttpResource}.
	 */
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
			SectionOutput notFoundOutput = designer.addSectionOutput(NOT_FOUND_OUTPUT_NAME, HttpPath.class.getName(),
					false);

			// Build store functions (in reverse order to get to chain input)
			SectionFlowSinkNode nextHandler = notFoundOutput;
			for (int i = 0; i < HttpResourceArchitectEmployer.this.httpResourceSources.size(); i++) {
				HttpResourceSource source = HttpResourceArchitectEmployer.this.httpResourceSources.get(i);

				// Add the store function
				String functionName = "store" + source.nameSuffix;
				SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName,
						new SendHttpFileManagedFunctionSource());
				SectionFunction function = namespace.addSectionFunction(functionName,
						SendHttpFileManagedFunctionSource.FUNCTION_NAME);

				// Configure the dependencies
				function.getFunctionObject(SendHttpFileFunction.Dependencies.HTTP_PATH.name()).flagAsParameter();
				designer.link(
						function.getFunctionObject(SendHttpFileFunction.Dependencies.SERVER_HTTP_CONNECTION.name()),
						serverHttpConnection);

				// Configure dependency to resources store
				SectionObject httpResourceStore = designer.addSectionObject(
						HttpResourceStore.class.getSimpleName() + source.nameSuffix, HttpResourceStore.class.getName());
				designer.link(function.getFunctionObject(SendHttpFileFunction.Dependencies.HTTP_RESOURCE_STORE.name()),
						httpResourceStore);

				// Configure as handling not available
				FunctionFlow notFoundFunctionFlow = function
						.getFunctionFlow(SendHttpFileFunction.Flows.NOT_AVAILABLE.name());
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

			// Build store functions (in reverse order to get to chain input)
			for (int i = 0; i < HttpResourceArchitectEmployer.this.httpResourceSources.size(); i++) {
				HttpResourceSource source = HttpResourceArchitectEmployer.this.httpResourceSources.get(i);

				// Add the cache function
				String functionName = "cache" + source.nameSuffix;
				SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace(functionName,
						new SendCachedHttpFileManagedFunctionSource());
				SectionFunction function = namespace.addSectionFunction(functionName,
						SendCachedHttpFileManagedFunctionSource.FUNCTION_NAME);

				// Configure the dependencies
				function.getFunctionObject(SendCachedHttpFileFunction.Dependencies.HTTP_PATH.name()).flagAsParameter();
				designer.link(
						function.getFunctionObject(
								SendCachedHttpFileFunction.Dependencies.SERVER_HTTP_CONNECTION.name()),
						serverHttpConnection);

				// Configure dependency to resource cache
				SectionObject httpResourceCache = designer.addSectionObject(
						HttpResourceCache.class.getSimpleName() + source.nameSuffix, HttpResourceCache.class.getName());
				designer.link(
						function.getFunctionObject(SendCachedHttpFileFunction.Dependencies.HTTP_RESOURCE_CACHE.name()),
						httpResourceCache);

				// Configure as handling not available
				FunctionFlow notFoundFunctionFlow = function
						.getFunctionFlow(SendCachedHttpFileFunction.Flows.NOT_CACHED.name());
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
			designer.link(
					serviceFunction
							.getFunctionObject(ServiceHttpRequestFunction.Dependencies.SERVER_HTTP_CONNECTION.name()),
					serverHttpConnection);
			designer.link(input, serviceFunction);
			designer.link(serviceFunction, nextHandler);

			// Configure triggering the resource
			Set<String> configuredResourcePaths = new HashSet<>();
			for (ResourceLink link : HttpResourceArchitectEmployer.this.resourceLinks) {
				this.configureResourceTrigger(link.resourcePath, nextHandler, designer, configuredResourcePaths);
			}
			for (EscalationResource escalation : HttpResourceArchitectEmployer.this.escalationResources) {
				this.configureResourceTrigger(escalation.resourcePath, nextHandler, designer, configuredResourcePaths);
			}

		}

		/**
		 * Configures the triggering of a resource.
		 * 
		 * @param resourcePath
		 *            Path for the resource.
		 * @param initialHandler
		 *            {@link SectionFunction} for the initial handler.
		 * @param designer
		 *            {@link SectionDesigner}.
		 * @param configuredResourcePaths
		 *            {@link Set} of existing configured resources.
		 */
		private void configureResourceTrigger(String resourcePath, SectionFlowSinkNode initialHandler,
				SectionDesigner designer, Set<String> configuredResourcePaths) {

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
			designer.link(function, initialHandler);

			// Configure handling trigger
			SectionInput send = designer.addSectionInput("send_" + resourcePath, null);
			designer.link(send, function);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link ServiceHttpRequestFunction}.
	 */
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
					.addManagedFunctionType(FUNCTION_NAME, new ServiceHttpRequestFunction(),
							ServiceHttpRequestFunction.Dependencies.class, None.class);
			function.addObject(ServerHttpConnection.class)
					.setKey(ServiceHttpRequestFunction.Dependencies.SERVER_HTTP_CONNECTION);
			function.setReturnType(HttpPath.class);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link SendCachedHttpFileFunction}.
	 */
	private static class SendCachedHttpFileManagedFunctionSource extends AbstractManagedFunctionSource {

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
			ManagedFunctionTypeBuilder<SendCachedHttpFileFunction.Dependencies, SendCachedHttpFileFunction.Flows> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, new SendCachedHttpFileFunction(),
							SendCachedHttpFileFunction.Dependencies.class, SendCachedHttpFileFunction.Flows.class);
			function.addObject(HttpPath.class).setKey(SendCachedHttpFileFunction.Dependencies.HTTP_PATH);
			function.addObject(HttpResourceCache.class).setKey(Dependencies.HTTP_RESOURCE_CACHE);
			function.addObject(ServerHttpConnection.class)
					.setKey(SendCachedHttpFileFunction.Dependencies.SERVER_HTTP_CONNECTION);
			ManagedFunctionFlowTypeBuilder<SendCachedHttpFileFunction.Flows> flow = function.addFlow();
			flow.setKey(SendCachedHttpFileFunction.Flows.NOT_CACHED);
			flow.setArgumentType(HttpPath.class);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link SendHttpFileFunction}.
	 */
	private static class SendHttpFileManagedFunctionSource extends AbstractManagedFunctionSource {

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
			ManagedFunctionTypeBuilder<SendHttpFileFunction.Dependencies, SendHttpFileFunction.Flows> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION_NAME, new SendHttpFileFunction(),
							SendHttpFileFunction.Dependencies.class, SendHttpFileFunction.Flows.class);
			function.addObject(HttpPath.class).setKey(SendHttpFileFunction.Dependencies.HTTP_PATH);
			function.addObject(HttpResourceStore.class).setKey(SendHttpFileFunction.Dependencies.HTTP_RESOURCE_STORE);
			function.addObject(ServerHttpConnection.class)
					.setKey(SendHttpFileFunction.Dependencies.SERVER_HTTP_CONNECTION);
			ManagedFunctionFlowTypeBuilder<SendHttpFileFunction.Flows> flow = function.addFlow();
			flow.setKey(SendHttpFileFunction.Flows.NOT_AVAILABLE);
			flow.setArgumentType(HttpPath.class);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the
	 * {@link TriggerSendHttpFileFunction}.
	 */
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
		 * @param path
		 *            Path.
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
			ManagedFunctionTypeBuilder<None, None> function = functionNamespaceTypeBuilder.addManagedFunctionType(
					FUNCTION_NAME, new TriggerSendHttpFileFunction(path), None.class, None.class);
			function.setReturnType(HttpPath.class);
		}
	}

}