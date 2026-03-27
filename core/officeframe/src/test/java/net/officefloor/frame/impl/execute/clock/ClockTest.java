/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.clock;

import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can source a {@link Clock}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClockTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can use default {@link ClockFactory}.
	 */
	public void testDefaultClock() throws Exception {

		// Obtain time with default clock
		Clock<Long> clock = this.getClock();
		long currentTimeSeconds = System.currentTimeMillis() / 1000;
		long clockTime = clock.getTime();

		// Ensure appropriate time
		long adjustTime = currentTimeSeconds + 50;
		assertTrue("Incorrect default clock (current: " + currentTimeSeconds + ", clock: " + clockTime + ")",
				Math.abs(clockTime - adjustTime) < 2);
	}

	/**
	 * Ensure can override {@link ClockFactory}.
	 */
	public void testOverrideClock() throws Exception {

		// Override the clock factory
		long mockCurrentTime = 1000;
		this.getOfficeFloorBuilder().setClockFactory(new MockClockFactory(mockCurrentTime));

		// Obtain the overridden clock
		Clock<Long> clock = this.getClock();

		// Ensure appropriate time
		assertEquals("Incorrect override clock", Long.valueOf(mockCurrentTime + 50), clock.getTime());
	}

	/**
	 * Obtains the {@link Clock}.
	 */
	private Clock<Long> getClock() throws Exception {

		// Construct managed object (obtaining clock)
		Closure<Clock<Long>> clock = new Closure<>();
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (
				context) -> clock.value = context.getManagedObjectSourceContext().getClock((time) -> time + 50);

		// Compile to source the clock
		this.constructOfficeFloor();

		// Return the clock
		return clock.value;
	}

}
