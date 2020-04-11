package net.officefloor.spring.webmvc;

import org.apache.catalina.Context;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link OfficeFloor} {@link ServletWebServerFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorServletWebServerFactory extends TomcatServletWebServerFactory {

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

		// Configuration complete
		try {
			SpringWebMvcExtension.completeServletConfigurationInterest();
		} catch (Exception ex) {
			throw new WebServerException("Unable to start Tomcat", ex);
		}

		// Return the web server for Tomcat
		return new WebServer() {

			@Override
			public void start() throws WebServerException {
				// Started by OfficeFloor
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