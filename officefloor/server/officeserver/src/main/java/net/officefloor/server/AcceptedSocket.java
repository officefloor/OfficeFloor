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
package net.officefloor.server;

import java.nio.channels.SocketChannel;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.server.protocol.CommunicationProtocol;
import net.officefloor.server.protocol.Connection;

/**
 * {@link SocketChannel} just accepted to become a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AcceptedSocket {

	/**
	 * Obtains the {@link SocketChannel} just established.
	 * 
	 * @return {@link SocketChannel} just established.
	 */
	SocketChannel getSocketChannel();

	/**
	 * Obtains the {@link CommunicationProtocol} for the {@link SocketChannel}.
	 * 
	 * @return {@link CommunicationProtocol} for the {@link SocketChannel}.
	 */
	CommunicationProtocol getCommunicationProtocol();

	/**
	 * Obtains the {@link ManagedObjectExecuteContext} to handle the
	 * {@link SocketChannel}.
	 * 
	 * @return {@link ManagedObjectExecuteContext} to handle the
	 *         {@link SocketChannel}.
	 */
	ManagedObjectExecuteContext<Indexed> getManagedObjectExecuteContext();

}