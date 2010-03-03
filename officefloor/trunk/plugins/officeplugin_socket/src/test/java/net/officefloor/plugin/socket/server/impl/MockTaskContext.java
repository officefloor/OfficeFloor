/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.impl.ConnectionImpl;
import net.officefloor.plugin.socket.server.impl.ConnectionManager;
import net.officefloor.plugin.socket.server.impl.SocketListener.SocketListenerDependencies;

/**
 * Test {@link TaskContext}.
 *
 * @author Daniel Sagenschneider
 */
public class MockTaskContext
		implements
		TaskContext<ConnectionManager<ConnectionHandler>, SocketListenerDependencies, Indexed> {

	/**
	 * {@link ConnectionManager}.
	 */
	private final ConnectionManager<ConnectionHandler> connectionManager;

	/**
	 * Parameter.
	 */
	private final ConnectionImpl<ConnectionHandler> parameter;

	/**
	 * Initiate.
	 *
	 * @param connectionManager
	 *            {@link ConnectionManager}.
	 * @param parameter
	 *            Parameter.
	 */
	public MockTaskContext(
			ConnectionManager<ConnectionHandler> connectionManager,
			ConnectionImpl<ConnectionHandler> parameter) {
		this.connectionManager = connectionManager;
		this.parameter = parameter;
	}

	/*
	 * =================== TaskContext ================================
	 */

	@Override
	public void setComplete(boolean isComplete) {
		// Ignore
	}

	@Override
	public FlowFuture doFlow(Indexed key, Object parameter) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public FlowFuture doFlow(int flowIndex, Object parameter) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public Object getObject(SocketListenerDependencies key) {
		return this.parameter;
	}

	@Override
	public Object getObject(int managedObjectIndex) {
		return this.parameter;
	}

	@Override
	public Object getProcessLock() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public ConnectionManager<ConnectionHandler> getWork() {
		return this.connectionManager;
	}

	@Override
	public void join(FlowFuture flowFuture, long timeout, Object token) {
		TestCase.fail("Should not be invoked");
	}

}