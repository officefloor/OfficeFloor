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
package sun.tools.jconsole;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.console.OfficeConsoleInitialContextFactory;

/**
 * {@link JConsole} implementation for {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeConsole extends JConsole {

	/**
	 * System properties to be setup for connecting to the
	 * {@link OfficeBuildingManager}.
	 */
	public static final Map<String, String> SETUP_SYSTEM_PROPERTIES;

	static {
		// Create the setup properties
		Map<String, String> setupSystemProperties = new HashMap<>();
		setupSystemProperties.put(Context.INITIAL_CONTEXT_FACTORY,
				OfficeConsoleInitialContextFactory.class.getName());
		setupSystemProperties.put(Context.URL_PKG_PREFIXES,
				"net.officefloor.console");
		SETUP_SYSTEM_PROPERTIES = Collections
				.unmodifiableMap(setupSystemProperties);
	}

	/**
	 * Trust store file.
	 */
	private static File trustStoreFile = null;

	/**
	 * Obtains the trust store {@link File}.
	 * 
	 * @return Trust store {@link File}.
	 */
	public static File getTrustStoreFile() {

		// Ensure have the trust store file
		if (trustStoreFile == null) {
			throw new IllegalStateException(
					"Trust Store file must be specified by "
							+ OfficeConsole.class.getSimpleName());
		}

		// Return the trust store file
		return trustStoreFile;
	}

	/**
	 * Trust store password.
	 */
	private static String trustStorePassword = null;

	/**
	 * Obtains the trust store password.
	 * 
	 * @return Trust store password.
	 */
	public static String getTrustStorePassword() {
		return trustStorePassword;
	}

	/**
	 * Initiate.
	 */
	public OfficeConsole() {
		super(false); // Do not need hotspot
	}

	/**
	 * Runs the {@link OfficeConsole}.
	 * 
	 * @param host
	 *            Name of host where the {@link OfficeBuildingManager} is
	 *            running. May be <code>null</code> for localhost.
	 * @param port
	 *            Port that the {@link OfficeBuildingManager} is running on.
	 * @param username
	 *            Username.
	 * @param password
	 *            Password.
	 * @param trustStoreFile
	 *            {@link File} containing the trust store content.
	 * @param trustStorePassword
	 *            Password to the trust store {@link File}. May be
	 *            <code>null</code> if no password is required.
	 */
	public void run(String host, int port, final String username,
			final String password, File trustStoreFile,
			String trustStorePassword) {

		// Specify Office Console InitialContextFactory setup
		OfficeConsole.trustStoreFile = trustStoreFile;
		OfficeConsole.trustStorePassword = trustStorePassword;

		// Setup up system properties for setting up connecting
		for (String propertyName : SETUP_SYSTEM_PROPERTIES.keySet()) {
			System.setProperty(propertyName,
					SETUP_SYSTEM_PROPERTIES.get(propertyName));
		}

		// Obtain the JMX Service URL to the OfficeBuilding
		final String officeBuildingJmxServiceUrl;
		try {

			// Obtain the JMX Server Url of the OfficeBuilding
			officeBuildingJmxServiceUrl = OfficeBuildingManager
					.getOfficeBuildingJmxServiceUrl(host, port).toString();

		} catch (Exception ex) {
			// Indicate error
			System.err
					.println("Failed obtaining the JMX Service URL to the OfficeBuilding");
			ex.printStackTrace(System.err);
			return; // Must have JMX Service URL to run
		}

		// Attempt to start OfficeConsole connected to OfficeBuilding
		// Always create Swing GUI on the Event Dispatching Thread
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {

		// Center the window on screen, taking into account screen
		// size and insets.
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		GraphicsConfiguration gc = this.getGraphicsConfiguration();
		Dimension scrSize = toolkit.getScreenSize();
		Insets scrInsets = toolkit.getScreenInsets(gc);
		Rectangle scrBounds = new Rectangle(scrInsets.left, scrInsets.top,
				scrSize.width - scrInsets.left - scrInsets.right,
				scrSize.height - scrInsets.top - scrInsets.bottom);
		int w = Math.min(900, scrBounds.width);
		int h = Math.min(750, scrBounds.height);
		this.setBounds(scrBounds.x + (scrBounds.width - w) / 2, scrBounds.y
				+ (scrBounds.height - h) / 2, w, h);

		this.setVisible(true);

		// Accessing privates of JConsole
		try {
			this.invoke(this.superMethod("createMDI"));

			// Create Proxy Client to Office Building
			ProxyClient proxyClient = ProxyClient.getProxyClient(
					officeBuildingJmxServiceUrl, username, password);

			// Create the VMPanel
			int updateInterval = 60 * 1000; // 60 seconds
			VMPanel vmPanel = new VMPanel(proxyClient, updateInterval);

			// Flag not check SSL (as configured differently)
			this.setFieldValue(vmPanel, "shouldUseSSL", Boolean.FALSE);

			// Add the Frame
			this.invoke(this.superMethod("addFrame", VMPanel.class), vmPanel);

			// Connect
			vmPanel.connect();

		} catch (Exception ex) {
			// Provide error
			System.err.println("Failed running "
					+ this.getClass().getSimpleName());
			ex.printStackTrace(System.err);

			// Hide console and destroy the console
			this.setVisible(false);
			this.dispose();
		}
		// }
		// });
	}

	/**
	 * Invokes the method with the parameters.
	 * 
	 * @param method
	 *            {@link Method}.
	 * @param arguments
	 *            Arguments.
	 * @return Possible return value from the {@link Method}.
	 * @throws Exception
	 *             If fails to invoke the {@link Method}.
	 */
	private Object invoke(Method method, Object... arguments) throws Exception {

		// Ensure the method is accessible
		method.setAccessible(true);

		// Undertake the method returning the possible result
		return method.invoke(this, arguments);
	}

	/**
	 * Obtains the method declare on {@link JConsole}.
	 * 
	 * @param methodName
	 *            Name of the {@link Method}.
	 * @param parameterTypes
	 *            Parameter types.
	 * @return {@link Method} declared on {@link JConsole}.
	 * @throws Exception
	 *             If fails to obtain the {@link Method}.
	 */
	private Method superMethod(String methodName, Class<?>... parameterTypes)
			throws Exception {

		// Obtain the method
		Method method = this.getClass().getSuperclass()
				.getDeclaredMethod(methodName, parameterTypes);

		// Return the method
		return method;
	}

	/**
	 * Specifies a field value on an object.
	 * 
	 * @param instance
	 *            Object.
	 * @param fieldName
	 *            Name of the {@link Field}.
	 * @param fieldValue
	 *            Value for the {@link Field}.
	 * @throws Exception
	 *             If fails to specify.
	 */
	private void setFieldValue(Object instance, String fieldName,
			Object fieldValue) throws Exception {

		// Obtain the field
		Field field = instance.getClass().getDeclaredField(fieldName);

		// Specify the field value
		field.setAccessible(true);
		field.set(instance, fieldValue);
	}

}