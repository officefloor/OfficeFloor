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
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.naming.Context;
import javax.swing.SwingUtilities;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.console.OfficeConsoleInitialContextFactory;
import sun.tools.jconsole.JConsole;
import sun.tools.jconsole.ProxyClient;

/**
 * {@link JConsole} implementation for {@link OfficeBuildingManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeConsole extends JConsole {

	/**
	 * Prefix of property names to configure the {@link OfficeConsole}.
	 */
	private static final String OFFICE_CONSOLE_PROPERTY_PREFIX = OfficeBuildingManager.OFFICE_BUILDING_ARTIFACT_ID
			+ ".officeconsole";

	/**
	 * Obtains the trust store {@link File}.
	 * 
	 * @return Trust store {@link File}.
	 */
	public static File getTrustStoreFile() {
		String trustStoreFilePath = System
				.getProperty(OFFICE_CONSOLE_PROPERTY_PREFIX
						+ ".trust.store.file");
		return new File(trustStoreFilePath);
	}

	/**
	 * Obtains the trust store password.
	 * 
	 * @return Trust store password.
	 */
	public static String getTrustStorePassword() {
		String trustStorePassword = System
				.getProperty(OFFICE_CONSOLE_PROPERTY_PREFIX
						+ ".trust.store.password");
		return trustStorePassword;
	}

	public static void main(String[] args) {

		// Setup up properties for connecting
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				OfficeConsoleInitialContextFactory.class.getName());
		System.setProperty(Context.URL_PKG_PREFIXES, "net.officefloor.console");

		final boolean hotspot = false;

		try {
			final String officeBuildingJmxServiceUrl = OfficeBuildingManager
					.getOfficeBuildingJmxServiceUrl(null, 13778).toString();

			// Always create Swing GUI on the Event Dispatching Thread
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					// Changed to OfficeConsole
					OfficeConsole jConsole = new OfficeConsole(hotspot);

					// Center the window on screen, taking into account screen
					// size and insets.
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					GraphicsConfiguration gc = jConsole
							.getGraphicsConfiguration();
					Dimension scrSize = toolkit.getScreenSize();
					Insets scrInsets = toolkit.getScreenInsets(gc);
					Rectangle scrBounds = new Rectangle(scrInsets.left,
							scrInsets.top, scrSize.width - scrInsets.left
									- scrInsets.right, scrSize.height
									- scrInsets.top - scrInsets.bottom);
					int w = Math.min(900, scrBounds.width);
					int h = Math.min(750, scrBounds.height);
					jConsole.setBounds(scrBounds.x + (scrBounds.width - w) / 2,
							scrBounds.y + (scrBounds.height - h) / 2, w, h);

					jConsole.setVisible(true);

					// Accessing privates of JConsole
					try {
						jConsole.invoke(jConsole.superMethod("createMDI"));

						// Create Proxy Client to Office Building
						ProxyClient proxyClient = ProxyClient.getProxyClient(
								officeBuildingJmxServiceUrl, "admin",
								"password");

						// Create the VMPanel
						int updateInterval = 60 * 1000; // 60 seconds
						VMPanel vmPanel = new VMPanel(proxyClient,
								updateInterval);

						// Flag not check SSL (as configured differently)
						jConsole.setFieldValue(vmPanel, "shouldUseSSL",
								Boolean.FALSE);

						// Add the Frame
						jConsole.invoke(
								jConsole.superMethod("addFrame", VMPanel.class),
								vmPanel);

						// Connect
						vmPanel.connect();

					} catch (Exception ex) {

						jConsole.getContentPane().add(
								new Label("OfficeConsole failure: "
										+ ex.getMessage()));
					}
				}
			});

		} catch (Exception ex) {
			// TODO provide log of failure
		}
	}

	private Object invoke(Method method, Object... arguments) throws Exception {

		// Ensure the method is accessible
		method.setAccessible(true);

		// Undertake the method returning the possible result
		return method.invoke(this, arguments);
	}

	private Method superMethod(String methodName, Class<?>... parameterTypes)
			throws Exception {

		// Obtain the method
		Method method = this.getClass().getSuperclass()
				.getDeclaredMethod(methodName, parameterTypes);

		// Return the method
		return method;
	}

	private void setFieldValue(Object instance, String fieldName,
			Object fieldValue) throws Exception {

		// Obtain the field
		Field field = instance.getClass().getDeclaredField(fieldName);

		// Specify the field value
		field.setAccessible(true);
		field.set(instance, fieldValue);
	}

	/**
	 * Initiate.
	 * 
	 * @param hotspot
	 *            Whether a hotspot.
	 */
	public OfficeConsole(boolean hotspot) {
		super(hotspot);
	}

}
