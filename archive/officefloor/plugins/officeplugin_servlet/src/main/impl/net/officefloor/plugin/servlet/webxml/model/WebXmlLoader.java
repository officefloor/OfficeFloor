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
package net.officefloor.plugin.servlet.webxml.model;

import java.io.InputStream;

import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;

/**
 * Loads the {@link WebAppModel} from the <code>web.xml</code> configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class WebXmlLoader {

	/**
	 * Loads the {@link WebAppModel}.
	 * 
	 * @param webXmlConfiguration
	 *            {@link InputStream} to the <code>web.xml</code> content.
	 * @param context
	 *            {@link SourceContext}.
	 * @return {@link WebAppModel}.
	 * @throws Exception
	 *             If fails to load configuration.
	 */
	public WebAppModel loadConfiguration(InputStream webXmlConfiguration,
			SourceContext context) throws Exception {

		// Obtain the unmarshaller for the web.xml configuration
		WebAppModel webApp = new WebAppModel();
		String unmarshallerLocation = webApp.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/UnmarshalWebXml.xml";
		InputStream unmarshallerConfiguration = context
				.getResource(unmarshallerLocation);
		XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
				.createUnmarshaller(unmarshallerConfiguration);

		// Unmarshal the web.xml configuration
		unmarshaller.unmarshall(webXmlConfiguration, webApp);

		// Return the web-app model
		return webApp;
	}

}