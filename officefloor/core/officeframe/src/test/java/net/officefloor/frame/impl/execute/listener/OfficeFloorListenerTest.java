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

package net.officefloor.frame.impl.execute.listener;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Tests the open/close hooks for {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorListenerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Mock {@link OfficeFloorListener}.
	 */
	private final MockOfficeFloorListener listener = new MockOfficeFloorListener();

	/**
	 * Ensure open {@link OfficeFloorListener} notified.
	 */
	public void testOpen() throws Exception {

		// Construct the OfficeFloor with listener
		this.getOfficeFloorBuilder().addOfficeFloorListener(this.listener);
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Ensure not yet notified
		this.listener.assertOpenEvents();

		// Open OfficeFloor
		officeFloor.openOfficeFloor();
		this.listener.assertOpenEvents(officeFloor);
	}

	/**
	 * Ensure able to register multiple {@link OfficeFloorListener} instances.
	 */
	public void testMultipleOpen() throws Exception {

		final MockOfficeFloorListener secondListener = new MockOfficeFloorListener();

		// Construct the OfficeFloor with listener
		this.getOfficeFloorBuilder().addOfficeFloorListener(this.listener);
		this.getOfficeFloorBuilder().addOfficeFloorListener(secondListener);
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Ensure not yet notified
		this.listener.assertOpenEvents();
		secondListener.assertOpenEvents();

		// Open OfficeFloor
		officeFloor.openOfficeFloor();
		this.listener.assertOpenEvents(officeFloor);
		secondListener.assertOpenEvents(officeFloor);
	}

	/**
	 * Ensure close {@link OfficeFloorListener} notified.
	 */
	public void testClose() throws Exception {

		// Construct the OfficeFloor with listener
		this.getOfficeFloorBuilder().addOfficeFloorListener(this.listener);
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Ensure not yet notified
		this.listener.assertCloseEvents();

		// Ensure still not notified of close on open
		officeFloor.openOfficeFloor();
		this.listener.assertCloseEvents();

		// Ensure notified of close
		officeFloor.closeOfficeFloor();
		this.listener.assertCloseEvents(officeFloor);
	}

	/**
	 * Ensure close {@link OfficeFloorListener} notified.
	 */
	public void testMultipleClose() throws Exception {

		final MockOfficeFloorListener secondListener = new MockOfficeFloorListener();

		// Construct the OfficeFloor with listener
		this.getOfficeFloorBuilder().addOfficeFloorListener(this.listener);
		this.getOfficeFloorBuilder().addOfficeFloorListener(secondListener);
		OfficeFloor officeFloor = this.constructOfficeFloor();

		// Ensure not yet notified
		this.listener.assertCloseEvents();
		secondListener.assertCloseEvents();

		// Ensure still not notified of close on open
		officeFloor.openOfficeFloor();
		this.listener.assertCloseEvents();
		secondListener.assertCloseEvents();

		// Ensure notified of close
		officeFloor.closeOfficeFloor();
		this.listener.assertCloseEvents(officeFloor);
		secondListener.assertCloseEvents(officeFloor);
	}

	/**
	 * Mock {@link OfficeFloorListener} for testing.
	 */
	private static class MockOfficeFloorListener implements OfficeFloorListener {

		/**
		 * Open {@link OfficeFloorEvent} instances.
		 */
		public final List<OfficeFloorEvent> openEvents = new LinkedList<>();

		/**
		 * Close {@link OfficeFloorEvent} instances.s
		 */
		public final List<OfficeFloorEvent> closeEvents = new LinkedList<>();

		/**
		 * Asserts the open {@link OfficeFloorEvent} instances.
		 * 
		 * @param expected
		 *            Expected {@link OfficeFloor} instances corresponding the
		 *            expected {@link OfficeFloorEvent} instances.
		 */
		public void assertOpenEvents(OfficeFloor... expected) {
			this.assetEvents(this.openEvents, expected);
		}

		/**
		 * Asserts the close {@link OfficeFloorEvent} instances.
		 * 
		 * @param expected
		 *            Expected {@link OfficeFloor} instances corresponding the
		 *            expected {@link OfficeFloorEvent} instances.
		 */
		public void assertCloseEvents(OfficeFloor... expected) {
			this.assetEvents(this.closeEvents, expected);
		}

		/**
		 * Asserts appropriate number of {@link OfficeFloorEvent} instances.
		 * 
		 * @param events
		 *            {@link OfficeFloorEvent} instances.
		 * @param expected
		 *            {@link OfficeFloor} instances corresponding the expected
		 *            {@link OfficeFloorEvent} instances.
		 */
		public void assetEvents(List<OfficeFloorEvent> events, OfficeFloor... expected) {
			assertEquals("Incorrect number of events", expected.length, events.size());
			for (int i = 0; i < expected.length; i++) {
				assertSame("Incorrect event " + i, expected[i], events.get(i).getOfficeFloor());
			}
		}

		/*
		 * ==================== OfficeFloorListener ====================
		 */

		@Override
		public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
			this.openEvents.add(event);
		}

		@Override
		public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
			this.closeEvents.add(event);
		}
	}

}
