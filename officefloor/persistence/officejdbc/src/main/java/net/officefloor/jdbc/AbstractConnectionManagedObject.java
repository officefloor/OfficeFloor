/*-
 * #%L
 * JDBC Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.jdbc;

import java.sql.Connection;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Connection} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractConnectionManagedObject implements ManagedObject {

	/**
	 * {@link Connection}.
	 */
	protected Connection connection = null;

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 * @throws Throwable If fails to obtain the {@link Connection}.
	 */
	protected abstract Connection getConnection() throws Throwable;

	/*
	 * ================= ManagedObject =====================
	 */

	@Override
	public Object getObject() throws Throwable {
		if (this.connection == null) {
			this.connection = this.getConnection();
		}
		return this.connection;
	}

}
