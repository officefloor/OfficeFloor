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
package net.officefloor.plugin.jms;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;

/**
 * Provides abstract functionality for a {@link ManagedObjectSource} for JMS.
 * 
 * @author Daniel Sagenschneider
 */
public class JmsUtil {

	/**
	 * Property name to obtain the class of the {@link JmsAdminObjectFactory}.
	 */
	public static final String JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY = "jms.adminobjectfactory";

	/**
	 * <p>
	 * Obtains the {@link JmsAdminObjectFactory} to create the JMS administered
	 * objects.
	 * <p>
	 * Separated out to allow overriding.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @return {@link JmsAdminObjectFactory} to create the JMS administered
	 *         objects.
	 * @throws Exception
	 *             If fails to create the {@link JmsAdminObjectFactory}.
	 */
	public static JmsAdminObjectFactory getJmsAdminObjectFactory(
			ManagedObjectSourceContext<?> context) throws Exception {

		// Obtain the name of the JMS admin object factory
		String className = context
				.getProperty(JMS_ADMIN_OBJECT_FACTORY_CLASS_PROPERTY);

		// Create an instance of the JMS admin object factory
		JmsAdminObjectFactory jmsAdminObjectFactory = (JmsAdminObjectFactory) Class
				.forName(className).newInstance();

		// Initiate the JMS admin object factory
		jmsAdminObjectFactory.init(context);

		// Return the configured JMS admin object factory
		return jmsAdminObjectFactory;
	}

	/**
	 * All access via static methods.
	 */
	private JmsUtil() {
	}

}