/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.clock.Clock;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests overriding the {@link ClockFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClockFactoryTest extends OfficeFrameTestCase {

	/**
	 * Ensure can use default {@link ClockFactory}.
	 */
	public void testDefaultClockFactory() throws Exception {
		long currentTimeSeconds = Instant.now().getEpochSecond();
		this.doClockTest(null, (clock) -> assertTrue("Incorrect default clock - " + clock.getTime(),
				Math.abs(clock.getTime() - currentTimeSeconds) < 2));
	}

	/**
	 * Ensure can override the {@link ClockFactory}.
	 */
	public void testOverrideClockFactory() throws Exception {
		long mockCurrentTime = 10000;
		this.doClockTest(new MockClockFactory(mockCurrentTime),
				(clock) -> assertEquals("Incorrect override clock", Long.valueOf(mockCurrentTime), clock.getTime()));
	}

	/**
	 * Ensure {@link OfficeFloorListener} implementing {@link ClockFactory} is
	 * registered automatically.
	 */
	public void testOfficeFloorListenerClockFactory() throws Exception {
		long mockCurrentTime = 10000;
		MockOfficeFloorListenerClockFactory clockFactory = new MockOfficeFloorListenerClockFactory(mockCurrentTime);
		this.doClockTest(clockFactory,
				(clock) -> assertEquals("Incorrect override clock", Long.valueOf(mockCurrentTime), clock.getTime()));
		assertTrue("Should notify clock factory that open", clockFactory.isOpened);
	}

	/**
	 * Undertakes the {@link ClockFactory} test.
	 * 
	 * @param clockFactory  {@link ClockFactory}. <code>null</code> to use default
	 *                      {@link ClockFactory}.
	 * @param validateClock Validates the {@link Clock}.
	 */
	private void doClockTest(ClockFactory clockFactory, Consumer<Clock<Long>> validateClock) throws Exception {

		// Assert the clock
		List<Clock<Long>> clocks = new LinkedList<>();
		Consumer<SourceContext> assertClock = (context) -> {
			if (!context.isLoadingType()) {
				Clock<Long> clock = context.getClock((time) -> time);
				validateClock.accept(clock);
				clocks.add(clock);
			}
		};

		// Create the compiler
		CompileOfficeFloor compile = new CompileOfficeFloor();
		if (clockFactory != null) {
			compile.getOfficeFloorCompiler().setClockFactory(clockFactory);
		}

		// Ensure clock available in various compiler contexts
		compile.officeFloor((context) -> assertClock.accept(context.getOfficeFloorSourceContext()));
		compile.office((context) -> assertClock.accept(context.getOfficeSourceContext()));
		compile.section((context) -> assertClock.accept(context.getSectionSourceContext()));

		// Ensure clock available in frame context
		compile.office((context) -> context.getOfficeArchitect().addOfficeManagedObjectSource("TEST",
				new MockManagedObjectSource(assertClock)));

		// Ensure all clocks are available
		try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
			assertEquals("Should create all clocks", 4, clocks.size());
		}
	}

	@TestSource
	private static class MockManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		private final Consumer<SourceContext> assertClock;

		private MockManagedObjectSource(Consumer<SourceContext> assertClock) {
			this.assertClock = assertClock;
		}

		/*
		 * =================== ManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());

			// Ensure can create clock
			this.assertClock.accept(context.getManagedObjectSourceContext());
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ======================= ManagedObject =========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	private static class MockOfficeFloorListenerClockFactory extends MockClockFactory implements OfficeFloorListener {

		private boolean isOpened = false;

		public MockOfficeFloorListenerClockFactory(long currentTimeSeconds) {
			super(currentTimeSeconds);
		}

		@Override
		public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
			this.isOpened = true;
		}

		@Override
		public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
			// Only need to know open to be registered
		}
	}

}
