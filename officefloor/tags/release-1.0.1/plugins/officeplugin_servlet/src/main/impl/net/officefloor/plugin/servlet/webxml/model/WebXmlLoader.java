/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.io.FileNotFoundException;
import java.io.InputStream;

import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
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
	 * @param webXmlLocation
	 *            Location of the <code>web.xml</code>.
	 * @param context
	 *            {@link ConfigurationContext}.
	 * @return {@link WebAppModel}.
	 * @throws Exception
	 *             If fails to load configuration.
	 */
	public WebAppModel loadConfiguration(String webXmlLocation,
			SectionSourceContext context) throws Exception {

		// Obtain the web.xml configuration
		ConfigurationItem webXmlConfigurationItem = context
				.getConfiguration(webXmlLocation);
		if (webXmlConfigurationItem == null) {
			throw new FileNotFoundException("Can not find configuration '"
					+ webXmlLocation + "'");
		}
		InputStream webXmlConfiguration = webXmlConfigurationItem
				.getConfiguration();

		// Obtain the unmarshaller for the web.xml configuration
		WebAppModel webApp = new WebAppModel();
		String unmarshallerLocation = webApp.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/UnmarshalWebXml.xml";
		ConfigurationItem unmarshallerConfiguration = context
				.getConfiguration(unmarshallerLocation);
		XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
				.createUnmarshaller(
						unmarshallerConfiguration.getConfiguration());

		// Unmarshal the web.xml configuration
		unmarshaller.unmarshall(webXmlConfiguration, webApp);

		// Return the web-app model
		return webApp;
	}

}