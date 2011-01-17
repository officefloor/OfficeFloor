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

package net.officefloor.plugin.jndi.ejb;

import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.jndi.context.ValidateWork;

/**
 * Mock EJB.
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
public class MockEjb implements MockEjbLocal, MockEjbRemote {

	/*
	 * ==================== EJB interfaces ==========================
	 */

	@Override
	public boolean runOfficeFloor() throws Exception {

		// Obtain the initial context
		Context initialContext = new InitialContext();

		// Obtain the OfficeFloor
		String jndiName = ValidateWork.getOfficeFloorJndiName(false);
		OfficeFloor officeFloor = (OfficeFloor) initialContext.lookup(jndiName);

		// Invoke the Work
		ValidateWork.reset();
		ValidateWork.invokeWork(officeFloor, null);

		// Return whether invoked
		return ValidateWork.isTaskInvoked();
	}

}