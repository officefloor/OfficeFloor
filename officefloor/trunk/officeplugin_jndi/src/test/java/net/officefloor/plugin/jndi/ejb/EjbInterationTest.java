/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.jndi.ejb;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.openejb.client.LocalInitialContextFactory;

/**
 * Ensure that {@link OfficeFloor} can be integrated into EJB.
 * 
 * @author Daniel Sagenschneider
 */
public class EjbInterationTest extends TestCase {

	/**
	 * Ensure that able to lookup an {@link OfficeFloor} from within an EJB.
	 */
	public void testObtainOfficeFloorWithinEjb() throws Exception {

		// Create the initial context
		Properties properties = new Properties();
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				LocalInitialContextFactory.class.getName());
		Context initialContext = new InitialContext(properties);

		// TODO obtain EJB that uses OfficeFloor
		fail("TODO obtain EJB that uses OfficeFloor");
	}

}