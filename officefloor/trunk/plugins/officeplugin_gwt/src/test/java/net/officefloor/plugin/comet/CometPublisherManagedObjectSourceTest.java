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
package net.officefloor.plugin.comet;

import java.lang.reflect.Proxy;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.comet.CometPublisherManagedObjectSource.Dependencies;
import net.officefloor.plugin.comet.api.CometSubscriber;
import net.officefloor.plugin.comet.spi.CometService;

/**
 * Tests the {@link CometPublisherManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometPublisherManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(CometPublisherManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(CometPublisher.class);
		type.addDependency(Dependencies.COMET_SERVICE, CometService.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				CometPublisherManagedObjectSource.class);
	}

	/**
	 * Validate loading notification.
	 */
	public void testLoadNotification() throws Throwable {

		final CometService service = this.createMock(CometService.class);

		// Record publishing a notification
		this.recordReturn(service, service.publishEvent(
				MockNotificationCometPublisher.class.getName(), null, null),
				Long.valueOf(1));

		// Test
		this.replayMockObjects();

		// Load the publisher factory
		CometPublisher factory = this.loadCometPublisher(service);

		// Create the publisher
		MockNotificationCometPublisher publisher = factory
				.createPublisher(MockNotificationCometPublisher.class);
		publisher.notificaiton();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Validate loading without a match key.
	 */
	public void testLoadWithoutMatchKey() throws Throwable {

		final CometService service = this.createMock(CometService.class);

		// Record publishing an event without match key
		this.recordReturn(service, service.publishEvent(
				MockCometPublisher.class.getName(), "TEST", null), Long
				.valueOf(1));

		// Test
		this.replayMockObjects();

		// Load the publisher factory
		CometPublisher factory = this.loadCometPublisher(service);

		// Create the publisher
		MockCometPublisher publisher = factory
				.createPublisher(MockCometPublisher.class);
		publisher.send("TEST");

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Validate loading with a match key.
	 */
	public void testLoadWithMatchKey() throws Throwable {

		final CometService service = this.createMock(CometService.class);

		// Record publishing an event without match key
		this.recordReturn(service,
				service.publishEvent(
						MockMatchKeyCometPublisher.class.getName(), "TEST",
						"MATCH_KEY"), Long.valueOf(1));

		// Test
		this.replayMockObjects();

		// Load the publisher factory
		CometPublisher factory = this.loadCometPublisher(service);

		// Create the publisher
		MockMatchKeyCometPublisher publisher = factory
				.createPublisher(MockMatchKeyCometPublisher.class);
		publisher.send("TEST", "MATCH_KEY");

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if creating {@link CometPublisher} {@link Proxy} that has
	 * too many parameters.
	 */
	public void testLoadWithTooManyParameters() throws Throwable {

		final CometService service = this.createMock(CometService.class);

		// Test
		this.replayMockObjects();

		// Load the publisher factory
		CometPublisher factory = this.loadCometPublisher(service);

		// Fail to create publisher as too many parameters
		MockTooManyParametersCometPublisher publisher = factory
				.createPublisher(MockTooManyParametersCometPublisher.class);
		try {
			publisher.send("EVENT", "MATCH_KEY", new Error(
					"Too many parameters"));
			fail("Should not be successful as too many parameters");
		} catch (IllegalStateException ex) {
			assertEquals(
					"Incorrect cause",
					MockTooManyParametersCometPublisher.class.getName()
							+ " has too many parameters for "
							+ CometSubscriber.class.getSimpleName()
							+ " publish method. At most should be event data and match key.",
					ex.getMessage());
		}

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Loads the {@link CometPublisher}.
	 * 
	 * @param cometService
	 *            {@link CometService}.
	 * @return Loaded {@link CometPublisher}.
	 */
	private CometPublisher loadCometPublisher(CometService cometService)
			throws Throwable {

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		CometPublisherManagedObjectSource source = loader
				.loadManagedObjectSource(CometPublisherManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.COMET_SERVICE, cometService);
		ManagedObject mo = user.sourceManagedObject(source);
		assertNotNull("Should have managed object", mo);

		// Ensure correct type
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof CometPublisher);
		CometPublisher factory = (CometPublisher) object;

		// Return the Comet Publisher factory
		return factory;
	}

	/**
	 * Mock interface for {@link CometPublisher} with no event data.
	 */
	public static interface MockNotificationCometPublisher extends
			CometSubscriber {
		void notificaiton();
	}

	/**
	 * Mock interface for {@link CometPublisher}.
	 */
	public static interface MockCometPublisher extends CometSubscriber {
		void send(String event);
	}

	/**
	 * Mock interface for {@link CometPublisher} with match key.
	 */
	public static interface MockMatchKeyCometPublisher extends CometSubscriber {
		void send(String event, String matchKey);
	}

	/**
	 * Mock interface for {@link CometPublisher} with too many parameters.
	 */
	public static interface MockTooManyParametersCometPublisher extends
			CometSubscriber {
		void send(String event, String matchKey, Error tooManyParameters);
	}

}