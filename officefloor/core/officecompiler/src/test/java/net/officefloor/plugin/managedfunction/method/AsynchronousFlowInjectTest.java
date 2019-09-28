/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.MockAsynchronousFlow;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests injecting {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure can inject {@link AsynchronousFlow}.
	 */
	public void testAsynchronousFlow() {
		AsynchronousFlowFunction instance = new AsynchronousFlowFunction();
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(instance, "method", null, null);
		MockAsynchronousFlow[] asyncFlows = result.getAsynchronousFlows();
		assertEquals("Should have async flow", 1, asyncFlows.length);
		assertSame("Incorrect async flow", asyncFlows[0], instance.asyncFlow);
	}

	public static class AsynchronousFlowFunction {

		public AsynchronousFlow asyncFlow;

		public void method(AsynchronousFlow asyncFlow) {
			this.asyncFlow = asyncFlow;
		}
	}

}