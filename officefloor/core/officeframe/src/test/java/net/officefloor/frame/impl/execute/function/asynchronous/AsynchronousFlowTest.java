/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.function.asynchronous;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to halt execution until {@link AsynchronousFlow} completes.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure able to halt execution until {@link AsynchronousFlow} completes.
	 */
	public void testAsynchronousFlow() throws Exception {

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "triggerAsynchronousFlow");
		trigger.buildAsynchronousFlow();
		trigger.setNextFunction("servicingComplete");
		this.constructFunction(work, "servicingComplete");

		// Ensure halts execution until flow completes
		this.triggerFunction("triggerAsynchronousFlow", null, null);
		assertFalse("Should halt on async flow and not complete servicing", work.isServicingComplete);

		// Complete flow confirming completes flow
		work.flow.complete(null);
		assertTrue("Should be complete servicing", work.isServicingComplete);
	}

	public class TestWork {

		private boolean isServicingComplete = false;

		private AsynchronousFlow flow;

		public void triggerAsynchronousFlow(AsynchronousFlow flow) {
			this.flow = flow;
		}

		public void servicingComplete() {
			this.isServicingComplete = true;
		}
	}

}
