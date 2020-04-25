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

package net.officefloor.spring.webflux;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.HttpHandler;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Web Flux {@link SpringSupplierExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebFluxSpringSupplierExtension implements SpringSupplierExtensionServiceFactory, SpringSupplierExtension {

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

		// Configure OfficeFloor embedded Web Flux
		builder.sources(OfficeFloorEmbeddedWebFluxConfiguration.class);
	}

	/**
	 * {@link Configuration} to embed {@link OfficeFloor}.
	 */
	@Configuration(proxyBeanMethods = false)
	public static class OfficeFloorEmbeddedWebFluxConfiguration {

		@Bean
		@Primary
		public ReactiveWebServerFactory reactiveWebServerFactory() {
			return new OfficeFloorReactiveWebServerFactory();
		}
	}

	public static class OfficeFloorReactiveWebServerFactory implements ReactiveWebServerFactory {

		@Override
		public WebServer getWebServer(HttpHandler httpHandler) {

			// Specify the handler
			WebFluxSectionSource.setHttpHandler(httpHandler);

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