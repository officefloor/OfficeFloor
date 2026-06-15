/*-
 * #%L
 * Spring Web MVC Integration
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

package net.officefloor.spring.webmvc;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.core.StandardContext;
import org.springframework.beans.factory.ObjectProvider;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.tomcat.TomcatContextCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.servlet.ServletContainerInitializer;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.spring.extension.AfterSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.BeforeSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Web MVC {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebMvcSpringSupplierExtension implements SpringSupplierExtensionServiceFactory, SpringSupplierExtension {

	/**
	 * {@link AvailableType} instances.
	 */
	private static final ThreadLocal<AvailableType[]> availableTypes = new ThreadLocal<>();

	/*
	 * ================== SpringSupplierExtensionServiceFactory =================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= SpringSupplierExtension ==========================
	 */

	@Override
	public void beforeSpringLoad(BeforeSpringLoadSupplierExtensionContext context) throws Exception {
		availableTypes.set(context.getAvailableTypes());
	}

	@Override
	public void configureSpring(SpringApplicationBuilder builder) throws Exception {

		// Configure OfficeFloor embedded Tomcat
		builder.sources(OfficeFloorEmbeddedTomcatConfiguration.class);
	}

	@Override
	public void afterSpringLoad(AfterSpringLoadSupplierExtensionContext context) throws Exception {
		availableTypes.remove();
	}

	/**
	 * {@link Configuration} to embed {@link OfficeFloor} managed Tomcat.
	 */
	@Configuration(proxyBeanMethods = false)
	public static class OfficeFloorEmbeddedTomcatConfiguration {

		/**
		 * @param contextCustomizers {@link TomcatContextCustomizer} instances.
		 * @return {@link TomcatServletWebServerFactory}.
		 */
		@Bean
		@Primary
		public TomcatServletWebServerFactory tomcatServletWebServerFactory(
				ObjectProvider<TomcatContextCustomizer> contextCustomizers) {
			TomcatServletWebServerFactory factory = new OfficeFloorServletWebServerFactory();
			factory.getContextCustomizers()
					.addAll(contextCustomizers.orderedStream().collect(Collectors.toList()));
			return factory;
		}
	}

	/**
	 * {@link OfficeFloor} {@link ServletWebServerFactory}.
	 */
	public static class OfficeFloorServletWebServerFactory extends TomcatServletWebServerFactory {

		/*
		 * ================== TomcatServletWebServerFactory ====================
		 */

		@Override
		public WebServer getWebServer(ServletContextInitializer... initializers) {

			// Obtain the servlet manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();
			Context context = servletManager.getContext();

			// Configure the context
			this.configureContext(context, Arrays.asList(initializers));
			this.postProcessContext(context);

			// OfficeFloor uses StandardContext (via Tomcat.addWebapp()) which includes
			// ContextConfig. ContextConfig scans the classpath via service loader and adds
			// third-party SCIs (e.g., SpringServletContainerInitializer from spring-web, which
			// causes jersey-spring's SpringWebApplicationInitializer to add ContextLoaderListener).
			// Spring Boot normally avoids this by using TomcatEmbeddedContext (which skips
			// ContextConfig service-loader scanning). We replicate that behaviour here by adding a
			// lifecycle listener that, after ContextConfig runs during CONFIGURE_START_EVENT, removes
			// all service-loaded SCIs — keeping only DeferredServletContainerInitializers.
			context.addLifecycleListener(event -> {
				if (Lifecycle.CONFIGURE_START_EVENT.equals(event.getType())
						&& event.getLifecycle() instanceof StandardContext ctx) {
					try {
						Field initializersField = StandardContext.class.getDeclaredField("initializers");
						initializersField.setAccessible(true);
						@SuppressWarnings("unchecked")
						Map<ServletContainerInitializer, Set<Class<?>>> initializerMap = (Map<ServletContainerInitializer, Set<Class<?>>>) initializersField
								.get(ctx);
						initializerMap.keySet()
								.removeIf(sci -> !sci.getClass().getName().equals(
										"org.springframework.boot.tomcat.servlet.DeferredServletContainerInitializers"));
					} catch (Exception ex) {
						throw new WebServerException("Failed to remove service-loaded SCIs", ex);
					}
				}
			});

			// Start servlet container (sets servlet context before bean initialization)
			try {
				AvailableType[] types = availableTypes.get();
				ServletSupplierSource.forceStartServletContainer(types);
			} catch (Exception ex) {
				throw new WebServerException("Failed to start " + ServletSupplierSource.class.getSimpleName(), ex);
			}

			// Return the web server
			return new WebServer() {

				@Override
				public void start() throws WebServerException {
					// already started
				}

				@Override
				public int getPort() {
					return 1;
				}

				@Override
				public void stop() throws WebServerException {
					// Stopped by OfficeFloor
				}
			};
		}
	}

}
