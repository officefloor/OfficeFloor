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
package net.officefloor.plugin.socket.server.impl;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownWorkException;

/**
 * Test {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockTaskContext implements ManagedFunctionContext<Work, None, None> {

	/**
	 * By default is complete.
	 */
	private boolean isComplete = true;

	/**
	 * Executes the {@link ManagedFunction}.
	 * 
	 * @throws Throwable
	 *             If failure of the {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void execute(ManagedFunction task) throws Throwable {
		this.isComplete = true;
		task.execute(this);
	}

	/**
	 * Indicates if complete.
	 * 
	 * @return <code>true</code> if complete.
	 */
	public boolean isComplete() {
		return this.isComplete;
	}

	/*
	 * =================== TaskContext ================================
	 */

	@Override
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	@Override
	public FlowFuture doFlow(None key, Object parameter) {
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
			throws UnknownWorkException, UnknownFunctionException,
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
	public Work getWork() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public void join(FlowFuture flowFuture, long timeout, Object token) {
		TestCase.fail("Should not be invoked");
	}

}