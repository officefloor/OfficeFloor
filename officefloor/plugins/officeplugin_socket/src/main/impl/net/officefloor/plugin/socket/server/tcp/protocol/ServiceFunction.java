/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;

/**
 * {@link ManagedFunction} to service the {@link TcpConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServiceFunction
		implements ManagedFunctionFactory<ServiceFunction.ServiceFunctionObjects, ServiceFunction.ServiceFunctionFlows>,
		ManagedFunction<ServiceFunction.ServiceFunctionObjects, ServiceFunction.ServiceFunctionFlows> {

	/**
	 * {@link Flow} keys for the {@link ServiceFunction}.
	 */
	public static enum ServiceFunctionFlows {
		SERVICE
	}

	/**
	 * Object keys for the {@link ServiceFunction}.
	 */
	public static enum ServiceFunctionObjects {
		CONNECTION
	}

	/*
	 * =================== ManagedFunctionFactory ===================
	 */

	@Override
	public ManagedFunction<ServiceFunctionObjects, ServiceFunctionFlows> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * ======================= ManagedFunction ======================
	 */

	@Override
	public Object execute(ManagedFunctionContext<ServiceFunctionObjects, ServiceFunctionFlows> context)
			throws Throwable {

		// Must wait on data
		ServerTcpConnection connection = (ServerTcpConnection) context.getObject(ServiceFunctionObjects.CONNECTION);

		// Service the connection
		context.doFlow(ServiceFunctionFlows.SERVICE, null, new ContinueFlowCallback(connection, context));

		// Only ends, once serviced the connection
		return null;
	}

	/**
	 * {@link FlowCallback} to continue execution until serviced
	 * {@link ServerTcpConnection}.
	 */
	private static class ContinueFlowCallback implements FlowCallback {

		/**
		 * {@link ServerTcpConnection}.
		 */
		private final ServerTcpConnection connection;

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<ServiceFunctionObjects, ServiceFunctionFlows> context;

		/**
		 * Instantiate.
		 * 
		 * @param connection
		 *            {@link ServerTcpConnection}.
		 * @param context
		 *            {@link ManagedFunctionContext}.
		 */
		public ContinueFlowCallback(ServerTcpConnection connection,
				ManagedFunctionContext<ServiceFunctionObjects, ServiceFunctionFlows> context) {
			this.connection = connection;
			this.context = context;
		}

		/*
		 * ================== FlowCallback ==========================
		 */

		@Override
		public void run(Throwable escalation) throws Throwable {

			// Close connection on failure in handling
			if (escalation != null) {
				this.connection.getOutputStream().close();
				return;
			}

			// Continue to service the connection
			this.context.doFlow(ServiceFunctionFlows.SERVICE, null, this);
		}
	}

}