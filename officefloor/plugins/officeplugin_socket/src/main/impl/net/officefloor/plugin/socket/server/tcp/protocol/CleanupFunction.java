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
package net.officefloor.plugin.socket.server.tcp.protocol;

import java.io.OutputStream;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;

/**
 * Cleans up the {@link ServerTcpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class CleanupFunction implements ManagedFunctionFactory<None, None>, ManagedFunction<None, None> {

	/*
	 * ================ ManagedFunctionFactory ====================
	 */

	@Override
	public ManagedFunction<None, None> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * ==================== ManagedFunction =======================
	 */

	@Override
	public Object execute(ManagedFunctionContext<None, None> context) throws Throwable {

		// Flag to close the connection
		RecycleManagedObjectParameter<TcpConnectionHandler> parameter = RecycleManagedObjectParameter
				.getRecycleManagedObjectParameter(context);
		TcpConnectionHandler connection = parameter.getManagedObject();
		OutputStream outputStream = connection.getOutputStream();
		outputStream.close();

		// No further processing
		return null;
	}

}