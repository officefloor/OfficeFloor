/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.impl;

import net.officefloor.plugin.socket.server.ConnectionActionEnum;
import net.officefloor.plugin.socket.server.ManagedConnection;
import net.officefloor.plugin.socket.server.WriteDataAction;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;

/**
 * {@link WriteDataAction} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WriteDataActionImpl implements WriteDataAction {

	/**
	 * {@link ManagedConnection}.
	 */
	private final ManagedConnection connection;

	/**
	 * {@link WriteBuffer} instances.
	 */
	private final WriteBuffer[] writeBuffers;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link ManagedConnection}.
	 * @param writeBuffers
	 *            {@link WriteBuffer} instances.
	 */
	public WriteDataActionImpl(ManagedConnection connection,
			WriteBuffer[] writeBuffers) {
		this.connection = connection;
		this.writeBuffers = writeBuffers;
	}

	/*
	 * ===================== WriteDataAction ============================
	 */

	@Override
	public ConnectionActionEnum getType() {
		return ConnectionActionEnum.WRITE_DATA;
	}

	@Override
	public ManagedConnection getConnection() {
		return this.connection;
	}

	@Override
	public WriteBuffer[] getData() {
		return this.writeBuffers;
	}

}