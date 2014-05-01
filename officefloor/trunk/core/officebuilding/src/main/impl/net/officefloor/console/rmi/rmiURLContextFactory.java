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
package net.officefloor.console.rmi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import net.officefloor.console.OfficeConsoleInitialContextFactory;

/**
 * <p>
 * {@link ObjectFactory} following naming conventions for package.
 * <p>
 * This avoids using an <code>rmiURLContextFactory</code> specific to the schema
 * (rmi). The default {@link OfficeConsoleInitialContextFactory} is therefore
 * used.
 * 
 * @author Daniel Sagenschneider
 */
public class rmiURLContextFactory implements ObjectFactory {

	/*
	 * ===================== ObjectFactory ============================
	 */

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception {

		// Return nothing to use default InitialContext
		return null;
	}

}