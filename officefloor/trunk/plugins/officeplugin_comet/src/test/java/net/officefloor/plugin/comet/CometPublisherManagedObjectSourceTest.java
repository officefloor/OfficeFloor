/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
	 * Validate loading.
	 */
	public void testLoad() throws Throwable {

		final CometService service = this.createMock(CometService.class);

		// Record publishing an event
		this.recordReturn(service, service.publishEvent(
				MockCometPublisher.class.getName(), "TEST", "MATCH_KEY"), Long
				.valueOf(1));

		// Test
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		CometPublisherManagedObjectSource source = loader
				.loadManagedObjectSource(CometPublisherManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.COMET_SERVICE, service);
		ManagedObject mo = user.sourceManagedObject(source);
		assertNotNull("Should have managed object", mo);

		// Ensure correct type
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof CometPublisher);
		CometPublisher factory = (CometPublisher) object;

		// Create the publisher
		MockCometPublisher publisher = factory.createPublisher(
				MockCometPublisher.class, "MATCH_KEY");
		publisher.send("TEST");

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock interface for {@link CometPublisher}.
	 */
	public static interface MockCometPublisher extends CometSubscriber {
		void send(String event);
	}

}