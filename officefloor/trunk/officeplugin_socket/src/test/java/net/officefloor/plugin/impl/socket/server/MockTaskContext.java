/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;

/**
 * Test {@link TaskContext}.
 * 
 * @author Daniel
 */
public class MockTaskContext implements
		TaskContext<Object, ConnectionManager, None, Indexed> {

	/**
	 * Parameter.
	 */
	private final ConnectionImpl<Indexed> parameter;

	/**
	 * Initiate.
	 * 
	 * @param parameter
	 *            Parameter.
	 */
	public MockTaskContext(ConnectionImpl<Indexed> parameter) {
		this.parameter = parameter;
	}

	/*
	 * =============================================================================
	 * TaskContext
	 * =============================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#setComplete(boolean)
	 */
	@Override
	public void setComplete(boolean isComplete) {
		// Ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getParameter()
	 */
	@Override
	public Object getParameter() {
		return this.parameter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#doFlow(java.lang.Enum,
	 *      java.lang.Object)
	 */
	@Override
	public FlowFuture doFlow(Indexed key, Object parameter) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#doFlow(int,
	 *      java.lang.Object)
	 */
	@Override
	public FlowFuture doFlow(int flowIndex, Object parameter) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getObject(java.lang.Enum)
	 */
	@Override
	public Object getObject(None key) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getObject(int)
	 */
	@Override
	public Object getObject(int managedObjectIndex) {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getProcessLock()
	 */
	@Override
	public Object getProcessLock() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getWork()
	 */
	@Override
	public ConnectionManager getWork() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#join(net.officefloor.frame.api.execute.FlowFuture)
	 */
	@Override
	public void join(FlowFuture flowFuture) {
		TestCase.fail("Should not be invoked");
	}

}
