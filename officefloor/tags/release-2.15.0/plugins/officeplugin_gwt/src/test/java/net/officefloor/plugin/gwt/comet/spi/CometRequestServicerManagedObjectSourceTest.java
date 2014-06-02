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
package net.officefloor.plugin.gwt.comet.spi;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.gwt.comet.internal.CometRequest;
import net.officefloor.plugin.gwt.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicer;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicerManagedObjectSource;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicerManagedObjectSource.Dependencies;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;

/**
 * Test the {@link CometRequestServicerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometRequestServicerManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(CometRequestServicerManagedObjectSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(CometRequestServicer.class);
		type.addDependency(Dependencies.SERVER_GWT_RPC_CONNECTION,
				ServerGwtRpcConnection.class, null);
		type.addDependency(Dependencies.COMET_SERVICE, CometService.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				CometRequestServicerManagedObjectSource.class);
	}

	/**
	 * Ensure can source.
	 */
	public void testSource() throws Throwable {

		final ServerGwtRpcConnection<?> connection = this
				.createMock(ServerGwtRpcConnection.class);
		final CometService cometService = this.createMock(CometService.class);

		// Test
		this.replayMockObjects();

		// Load the comet request servicer
		this.loadCometRequestServicer(connection, cometService);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can service the {@link CometRequest}.
	 */
	@SuppressWarnings("unchecked")
	public void testServiceCometRequest() throws Throwable {

		final ServerGwtRpcConnection<CometResponse> connection = this
				.createMock(ServerGwtRpcConnection.class);
		final CometService cometService = this.createMock(CometService.class);

		// Record servicing the request
		cometService.service(connection);

		// Test
		this.replayMockObjects();

		// Load the comet request servicer
		CometRequestServicer servicer = this.loadCometRequestServicer(
				connection, cometService);

		// Service the request
		servicer.service();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Loads the {@link CometRequestServicer}.
	 * 
	 * @param connection
	 *            Mock {@link ServerGwtRpcConnection}.
	 * @param cometService
	 *            Mock {@link CometService}.
	 * @return Loaded {@link CometRequestServicer}.
	 */
	private CometRequestServicer loadCometRequestServicer(
			ServerGwtRpcConnection<?> connection, CometService cometService)
			throws Throwable {

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		CometRequestServicerManagedObjectSource source = loader
				.loadManagedObjectSource(CometRequestServicerManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.SERVER_GWT_RPC_CONNECTION, connection);
		user.mapDependency(Dependencies.COMET_SERVICE, cometService);
		ManagedObject mo = user.sourceManagedObject(source);

		// Obtain the object
		Object object = mo.getObject();
		assertTrue("Incorrect object type",
				object instanceof CometRequestServicer);

		// Return the comet request servicer
		return (CometRequestServicer) object;
	}

}