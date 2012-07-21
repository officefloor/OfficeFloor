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

import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.plugin.socket.server.ConnectionHandler;

/**
 * Test {@link TaskContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockTaskContext implements
		TaskContext<SocketListener<ConnectionHandler>, None, Indexed> {

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
	public void doFlow(String workName, String taskName, Object parameter)
			throws UnknownWorkException, UnknownTaskException,
			InvalidParameterTypeException {
		TestCase.fail("Should not be invoked");
	}

	@Override
	public Object getObject(None key) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public Object getObject(int managedObjectIndex) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public Object getProcessLock() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public SocketListener<ConnectionHandler> getWork() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public void join(FlowFuture flowFuture, long timeout, Object token) {
		TestCase.fail("Should not be invoked");
	}

}