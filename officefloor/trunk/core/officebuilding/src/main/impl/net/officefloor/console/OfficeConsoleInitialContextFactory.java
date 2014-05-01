/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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
package net.officefloor.console;

import java.io.File;
import java.util.Hashtable;

import javax.management.remote.JMXConnector;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingRmiClientSocketFactory;
import net.officefloor.building.manager.OfficeBuildingRmiServerSocketFactory;
import sun.tools.jconsole.OfficeConsole;

/**
 * {@link InitialContextFactory} for the {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeConsoleInitialContextFactory implements
		InitialContextFactory {

	/*
	 * ================ InitialContextFactory ========================
	 */

	@Override
	@SuppressWarnings("restriction")
	public Context getInitialContext(Hashtable<?, ?> environment)
			throws NamingException {

		try {

			// Create a clone of the environment
			Hashtable<String, Object> officeBuildingEnvironment = new Hashtable<>();

			// Obtain details for login
			String sslProtocol = OfficeBuildingManager.getSslProtocol();
			String sslAlgorithm = OfficeBuildingManager.getSslAlgorithm();
			File trustStoreFile = OfficeConsole.getTrustStoreFile();
			String trustStorePassword = OfficeConsole.getTrustStorePassword();

			// Obtain the trust store content
			byte[] trustStoreContent = OfficeBuildingRmiServerSocketFactory
					.getKeyStoreContent(trustStoreFile);

			// Add in the client socket factory
			OfficeBuildingRmiClientSocketFactory clientSocketFactory = new OfficeBuildingRmiClientSocketFactory(
					sslProtocol, sslAlgorithm, trustStoreContent,
					trustStorePassword);
			officeBuildingEnvironment.put("com.sun.jndi.rmi.factory.socket",
					clientSocketFactory);

			// Add in the credentials
			officeBuildingEnvironment.put(JMXConnector.CREDENTIALS,
					new String[] { "admin", "password" });

			// Return the initial context
			return new com.sun.jndi.url.rmi.rmiURLContext(
					officeBuildingEnvironment);

		} catch (Exception ex) {
			throw new NamingException();
		}
	}

}