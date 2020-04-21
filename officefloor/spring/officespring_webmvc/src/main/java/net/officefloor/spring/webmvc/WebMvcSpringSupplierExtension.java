/*-
 * #%L
 * Spring Web MVC Integration
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

package net.officefloor.spring.webmvc;

import java.util.stream.Collectors;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Web MVC {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebMvcSpringSupplierExtension implements SpringSupplierExtensionServiceFactory, SpringSupplierExtension {

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
	public void configureSpring(SpringApplicationBuilder builder) throws Exception {

		// Configure OfficeFloor embedded Tomcat
		builder.sources(OfficeFloorEmbeddedTomcatConfiguration.class);
	}

	/**
	 * {@link Configuration} to embed {@link OfficeFloor} managed {@link Tomcat}.
	 */
	@Configuration(proxyBeanMethods = false)
	public static class OfficeFloorEmbeddedTomcatConfiguration {

		@Bean
		@Primary
		public TomcatServletWebServerFactory tomcatServletWebServerFactory(
				ObjectProvider<TomcatContextCustomizer> contextCustomizers) {
			TomcatServletWebServerFactory factory = new OfficeFloorServletWebServerFactory();
			factory.getTomcatContextCustomizers()
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
			ServletContextInitializer[] initializersToUse = this.mergeInitializers(initializers);
			this.configureContext(context, initializersToUse);
			this.postProcessContext(context);

			// Ensure start servlet container
			try {
				ServletSupplierSource.forceStartServletContainer();
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
