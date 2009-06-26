/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.spi;

import java.io.IOException;

import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;

/**
 * <p>
 * {@link Server} to input {@link ReadMessage}.
 * <p>
 * Required to be implemented by the handler provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface Server<F extends Enum<F>> {

	/**
	 * Provides the {@link Server} the {@link ManagedObjectExecuteContext} to
	 * enable it to invoke {@link ProcessState} instances to process
	 * {@link ReadMessage}.
	 * 
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 */
	void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<F> executeContext);

	/**
	 * Starts the processing the {@link ReadMessage} with the {@link Server}.
	 * 
	 * @param message
	 *            {@link ReadMessage}.
	 * @param connectionHandler
	 *            {@link ConnectionHandler} for the {@link Connection} of the
	 *            {@link ReadMessage}.
	 * @throws IOException
	 *             If fails to process the {@link ReadMessage}.
	 */
	void processReadMessage(ReadMessage message,
			ConnectionHandler connectionHandler) throws IOException;

}