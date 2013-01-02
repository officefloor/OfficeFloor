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
package net.officefloor.plugin.socket.server.protocol;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;

/**
 * <p>
 * {@link CommunicationProtocol} to handle the input.
 * <p>
 * Required to be implemented by the handler provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface CommunicationProtocol {

	/**
	 * Provides the {@link CommunicationProtocol} the
	 * {@link ManagedObjectExecuteContext} to enable it to invoke
	 * {@link ProcessState} instances to process input.
	 * 
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext);

	/**
	 * Creates a {@link ConnectionHandler} for a new {@link Connection}.
	 * 
	 * @param connection
	 *            A new {@link Connection} requiring handling.
	 * @return {@link ConnectionHandler} to handle the new {@link Connection}.
	 */
	ConnectionHandler createConnectionHandler(Connection connection);

}