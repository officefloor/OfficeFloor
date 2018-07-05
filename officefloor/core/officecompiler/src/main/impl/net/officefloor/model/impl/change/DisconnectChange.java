/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.model.impl.change;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;

/**
 * {@link Change} to simplify removing {@link ConnectionModel} instances.
 *
 * @author Daniel Sagenschneider
 */
public abstract class DisconnectChange<T> extends AbstractChange<T> {

	/**
	 * {@link ConnectionModel} instances removed.
	 */
	private ConnectionModel[] connections;

	/**
	 * Initiate.
	 *
	 * @param target
	 *            Target to remove {@link ConnectionModel} instances.
	 */
	public DisconnectChange(T target) {
		super(target, "Remove connections for "
				+ target.getClass().getSimpleName());
	}

	/*
	 * ======================== Change =================================
	 */

	@Override
	public void apply() {
		// Populate the removed connections
		List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
		this.populateRemovedConnections(connList);
		this.connections = connList.toArray(new ConnectionModel[0]);
	}

	/**
	 * Populates the removed {@link ConnectionModel}.
	 *
	 * @param connList
	 *            List to populate with the removed {@link ConnectionModel}
	 *            instances.
	 */
	protected abstract void populateRemovedConnections(
			List<ConnectionModel> connList);

	@Override
	public void revert() {
		// Re-connect connections in reverse order
		for (int i = (this.connections.length - 1); i >= 0; i--) {
			this.connections[i].connect();
		}
	}
}