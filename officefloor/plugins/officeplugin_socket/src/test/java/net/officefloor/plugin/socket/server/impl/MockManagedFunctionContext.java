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

import org.junit.Assert;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;

/**
 * Test {@link ManagedFunctionContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockManagedFunctionContext<O extends Enum<O>, F extends Enum<F>> implements ManagedFunctionContext<O, F> {

	/**
	 * By default is complete.
	 */
	private boolean isComplete = true;

	/**
	 * Executes the {@link ManagedFunction}.
	 * 
	 * @param function
	 *            {@link ManagedFunction}.
	 * @throws Throwable
	 *             If failure of the {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void execute(ManagedFunction function) throws Throwable {
		this.isComplete = true;
		function.execute(this);
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
	 * =================== ManagedFunctionContext ========================
	 */

	@Override
	public void doFlow(F key, Object parameter, FlowCallback callback) {
		this.isComplete = false;
	}

	@Override
	public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {
		Assert.fail("Should not invoke indexed flow");
	}

	@Override
	public void doFlow(String functionkName, Object parameter, FlowCallback callback)
			throws UnknownFunctionException, InvalidParameterTypeException {
		Assert.fail("Should not invoke dynamic flow");
	}

	@Override
	public Object getObject(O key) {
		Assert.fail("Should not require object");
		return null;
	}

	@Override
	public Object getObject(int dependencyIndex) {
		Assert.fail("Should not require object");
		return null;
	}

}