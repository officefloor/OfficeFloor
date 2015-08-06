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
package net.officefloor.plugin.woof.servlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.host.ServletServerManagedObjectSource;
import net.officefloor.plugin.servlet.security.HttpServletSecurity;
import net.officefloor.plugin.servlet.security.HttpServletSecurityManagedObjectSource;
import net.officefloor.plugin.servlet.webxml.InvalidServletConfigurationException;
import net.officefloor.plugin.servlet.webxml.WebXmlSectionSource;
import net.officefloor.plugin.web.http.application.HttpSecurityAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpSecurityAutoWireSectionImpl;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.woof.WoofApplicationExtensionService;
import net.officefloor.plugin.woof.WoofApplicationExtensionServiceContext;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

/**
 * {@link WoofApplicationExtensionService} to chain in a {@link Servlet}
 * container servicer.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContainerWoofApplicationExtensionService implements
		WoofApplicationExtensionService {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ServletContainerWoofApplicationExtensionService.class
					.getName());

	/*
	 * ==================== WoofApplicationExtensionService ==================
	 */

	@Override
	public void extendApplication(WoofApplicationExtensionServiceContext context)
			throws Exception {

		// Process scope managed objects
		final ManagedObjectSourceWirer processScopeWirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.setManagedObjectScope(ManagedObjectScope.PROCESS);
			}
		};

		// Obtain the web application
		WebAutoWireApplication application = context.getWebApplication();

		// Obtain the web.xml configuration
		InputStream webXmlConfiguration = context
				.getOptionalResource(WoofOfficeFloorSource.WEBXML_FILE_PATH);
		if (webXmlConfiguration == null) {
			return; // No web.xml file so no extension
		}

		// Obtain content of web.xml file
		Reader webXmlReader = new InputStreamReader(webXmlConfiguration);
		StringWriter webXmlContent = new StringWriter();
		for (int value = webXmlReader.read(); value != -1; value = webXmlReader
				.read()) {
			webXmlContent.write(value);
		}
		webXmlReader.close();
		webXmlConfiguration.close();
		String webXml = webXmlContent.toString();

		// Validate that web.xml content is valid
		try {
			WebXmlSectionSource.validateWebXmlConfiguration(
					new ByteArrayInputStream(webXml.getBytes()), context);
		} catch (InvalidServletConfigurationException ex) {
			// Log invalid and not load servlet container
			if (LOGGER.isLoggable(Level.WARNING)) {
				LOGGER.log(
						Level.WARNING,
						"Invalid " + WoofOfficeFloorSource.WEBXML_FILE_PATH
								+ " so not loading Servlet servicers: "
								+ ex.getMessage());
			}
			return;
		}

		// Configure in the Servlet web.xml functionality
		AutoWireSection section = application.addSection("SERVLET",
				WebXmlSectionSource.class.getName(), null);
		section.addProperty(WebXmlSectionSource.PROPERTY_WEB_XML_CONFIGURATION,
				webXml);

		// Ensure Servlet Server is available
		final AutoWire servletServer = new AutoWire(ServletServer.class);
		if (!(application.isObjectAvailable(servletServer))) {
			// Configure the Servlet Server
			application.addManagedObject(
					ServletServerManagedObjectSource.class.getName(),
					processScopeWirer, servletServer);
		}

		// Ensure HTTP Servlet Security is available
		AutoWire httpServletSecurity = new AutoWire(HttpServletSecurity.class);
		if (!(application.isObjectAvailable(httpServletSecurity))) {
			// Configure the HTTP Servlet Security
			AutoWireObject httpServletSecurityObject = application
					.addManagedObject(
							HttpServletSecurityManagedObjectSource.class
									.getName(), processScopeWirer,
							httpServletSecurity);

			// Provide time out
			HttpSecurityAutoWireSection security = application
					.getHttpSecurity();
			long timeout = (security != null ? security.getSecurityTimeout()
					: HttpSecurityAutoWireSectionImpl.DEFAULT_HTTP_SECURITY_TIMEOUT);
			httpServletSecurityObject.setTimeout(timeout);
		}

		// Chain in the Servlet Container as a servicer
		application.chainServicer(section, WebXmlSectionSource.SERVICE_INPUT,
				WebXmlSectionSource.UNHANDLED_OUTPUT);
	}
}