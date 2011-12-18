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
package net.officefloor.plugin.comet.spi;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.comet.spi.CometServiceManagedObject.Dependencies;
import net.officefloor.plugin.comet.spi.CometServiceManagedObjectSource.Flows;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;

/**
 * Tests the {@link CometServiceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometServiceManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(CometServiceManagedObjectSource.class);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(CometService.class);
		type.addDependency(Dependencies.SERVER_GWT_RPC_CONNECTION,
				ServerGwtRpcConnection.class, null);
		type.addTeam("EXPIRE_TEAM");

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				CometServiceManagedObjectSource.class);
	}

	/**
	 * Ensure can source.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testSource() throws Throwable {

		final ServerGwtRpcConnection<?> connection = this
				.createMock(ServerGwtRpcConnection.class);
		final Task task = this.createMock(Task.class);
		final TaskContext taskContext = this.createMock(TaskContext.class);

		// Record sourcing
		this.recordReturn(task, task.doTask(taskContext), null);

		// Test
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.registerInvokeProcessTask(Flows.EXPIRE, task, taskContext);
		CometServiceManagedObjectSource source = loader
				.loadManagedObjectSource(CometServiceManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.SERVER_GWT_RPC_CONNECTION, connection);
		ManagedObject mo = user.sourceManagedObject(source);

		// Obtain the object
		Object object = mo.getObject();
		assertTrue("Incorrect object type", object instanceof CometService);

		// Verify
		this.verifyMockObjects();
	}

}