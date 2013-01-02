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
package net.officefloor.plugin.hibernate;

import java.sql.Connection;

import org.hibernate.Session;

import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.hibernate.HibernateManagedObjectSource.HibernateDependenciesEnum;

/**
 * {@link ManagedObject} for the Hibernate {@link Session}.
 * 
 * @author Daniel Sagenschneider
 */
public class HibernateManagedObject implements ManagedObject,
		CoordinatingManagedObject<HibernateDependenciesEnum> {

	/**
	 * {@link Session}.
	 */
	private final Session session;

	/**
	 * Initiate.
	 * 
	 * @param session
	 *            {@link Session}.
	 */
	public HibernateManagedObject(Session session) {
		this.session = session;
	}

	/*
	 * ==================== ManagedObject =================================
	 */

	@Override
	public Object getObject() throws Exception {
		return this.session;
	}

	/*
	 * ================= CoordinatingManagedObject ========================
	 */

	@Override
	public void loadObjects(ObjectRegistry<HibernateDependenciesEnum> registry)
			throws Exception {
		// Obtain the connection
		Connection connection = (Connection) registry
				.getObject(HibernateDependenciesEnum.CONNECTION);

		// Load the connection onto the session
		this.session.reconnect(connection);
	}

}