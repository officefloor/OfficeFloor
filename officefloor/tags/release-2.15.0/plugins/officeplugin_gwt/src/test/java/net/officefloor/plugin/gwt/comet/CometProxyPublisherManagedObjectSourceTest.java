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
package net.officefloor.plugin.gwt.comet;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.gwt.comet.CometProxyPublisherManagedObjectSource;
import net.officefloor.plugin.gwt.comet.CometPublisher;
import net.officefloor.plugin.gwt.comet.CometProxyPublisherManagedObjectSource.Dependencies;
import net.officefloor.plugin.gwt.comet.api.CometSubscriber;

/**
 * Tests the {@link CometProxyPublisherManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometProxyPublisherManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						CometProxyPublisherManagedObjectSource.class,
						CometProxyPublisherManagedObjectSource.PROPERTY_PROXY_INTERFACE,
						"Interface");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockCometSubscriber.class);
		type.addDependency(Dependencies.COMET_PUBLISHER, CometPublisher.class,
				null);

		// Validate the expected type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						CometProxyPublisherManagedObjectSource.class,
						CometProxyPublisherManagedObjectSource.PROPERTY_PROXY_INTERFACE,
						MockCometSubscriber.class.getName());
	}

	/**
	 * Validate load.
	 */
	public void testLoad() throws Throwable {

		final CometPublisher publisher = this.createMock(CometPublisher.class);
		final MockCometSubscriber mockProxy = this
				.createMock(MockCometSubscriber.class);

		// Record
		this.recordReturn(publisher,
				publisher.createPublisher(MockCometSubscriber.class), mockProxy);
		mockProxy.publish("TEST", "MATCH_KEY");

		// Test
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				CometProxyPublisherManagedObjectSource.PROPERTY_PROXY_INTERFACE,
				MockCometSubscriber.class.getName());
		CometProxyPublisherManagedObjectSource source = loader
				.loadManagedObjectSource(CometProxyPublisherManagedObjectSource.class);

		// Source the object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.COMET_PUBLISHER, publisher);
		ManagedObject managedObject = user.sourceManagedObject(source);
		Object object = managedObject.getObject();
		assertTrue("Should be a proxy", object instanceof MockCometSubscriber);
		MockCometSubscriber proxy = (MockCometSubscriber) object;

		// Publish an event
		proxy.publish("TEST", "MATCH_KEY");

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link CometSubscriber}.
	 */
	public static interface MockCometSubscriber extends CometSubscriber {
		void publish(String event, String matchKey);
	}

}