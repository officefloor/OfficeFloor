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

	/**
	 * {@link OfficeFloor} {@link ReactiveWebServerFactory}.
	 */
	public static class OfficeFloorReactiveWebServerFactory implements ReactiveWebServerFactory {

		/*
		 * ============== ReactiveWebServerFactory ==============
		 */

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