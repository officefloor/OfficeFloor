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

package net.officefloor.frame.impl.execute.managedobject.input;

import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can report {@link Escalation} instances of {@link ManagedObject}
 * instances back to the invoking {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class ReportEscalationToManagedObjectInvocationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can be notified of another {@link ManagedObject} failure in recycle.
	 */
	public void testNotifiedOfOtherManagedObjectFailure() throws Exception {

		// Construct the child managed object (dependent on parent)
		TestObject child = new TestObject("CHILD", this);
		Closure<CleanupEscalation[]> cleanupEscalations = new Closure<>();
		child.isRecycleFunction = true;
		child.recycleConsumer = (parameter) -> cleanupEscalations.value = parameter.getCleanupEscalations();
		this.bindManagedObject("CHILD", ManagedObjectScope.PROCESS, null);

		// Construct the parent managed object (cleaned up first)
		TestObject parent = new TestObject("PARENT", this);
		parent.isRecycleFunction = true;
		parent.recycleFailure = new SQLException("TEST");
		parent.isCoordinatingManagedObject = true;
		parent.enhanceMetaData = (metaData) -> metaData.addDependency(TestObject.class);
		this.bindManagedObject("PARENT", ManagedObjectScope.PROCESS, null).mapDependency(0, "CHILD");

		// Construct the functions
		TestWork work = new TestWork(parent, child);
		this.constructFunction(work, "task").buildObject("PARENT");

		// Test
		this.invokeFunctionAndValidate("task", null, "task");

		// Ensure recycle escalation reported to parent
		assertEquals("Incorrect number of clean up escalations", 1, cleanupEscalations.value.length);
		assertEquals("Incorrect escalation object type", TestObject.class, cleanupEscalations.value[0].getObjectType());
		assertSame("Incorrect cause of escalation", parent.recycleFailure, cleanupEscalations.value[0].getEscalation());
	}

	/**
	 * "real-world" use of this feature is to notify the {@link SocketChannel}
	 * {@link ManagedObject} of failure to commit changes by the Entity Manager
	 * close on recycle (i.e. applying changes within request to database
	 * {@link Connection}).
	 * 
	 * Order of dependency clean-up is consistent as {@link SocketChannel}
	 * {@link ManagedObject} typically bound to {@link ProcessState}, while Entity
	 * Manager bound to {@link ThreadState}.
	 */
	public void testNotifySocketConnectionOfEntityManagerFailure() throws Exception {

		// Construct the mock socket managed object
		TestObject socket = new TestObject("SOCKET", this);
		Closure<CleanupEscalation[]> cleanupEscalations = new Closure<>();
		socket.isRecycleFunction = true;
		socket.recycleConsumer = (parameter) -> cleanupEscalations.value = parameter.getCleanupEscalations();
		socket.enhanceMetaData = (metaData) -> metaData.addFlow(TestObject.class);
		this.bindManagedObject("SOCKET", ManagedObjectScope.PROCESS, null);
		socket.managingOfficeBuilder.setInputManagedObjectName("SOCKET");
		socket.managingOfficeBuilder.linkFlow(0, "task");

		// Construct the mock entity manager
		TestObject entityManager = new TestObject("ENTITY_MANAGER", this);
		entityManager.isRecycleFunction = true;
		entityManager.recycleFailure = new SQLException("TEST");
		entityManager.isCoordinatingManagedObject = true;
		this.bindManagedObject("ENTITY_MANAGER", ManagedObjectScope.THREAD, null);

		// Construct the functions
		TestWork work = new TestWork(entityManager, null);
		this.constructFunction(work, "task").buildObject("ENTITY_MANAGER");

		// Construct the OfficeFloor
		this.constructOfficeFloor().openOfficeFloor();

		// Mock handle the socket request
		CompleteFlowCallback complete = new CompleteFlowCallback();
		socket.managedObjectServiceContext.invokeProcess(0, socket, socket, 0, complete);
		complete.assertComplete();

		// Ensure recycle escalation reported to parent
		assertEquals("Incorrect number of clean up escalations", 1, cleanupEscalations.value.length);
		assertEquals("Incorrect escalation object type", TestObject.class, cleanupEscalations.value[0].getObjectType());
		assertSame("Incorrect cause of escalation", entityManager.recycleFailure,
				cleanupEscalations.value[0].getEscalation());
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final TestObject parent;

		private final TestObject child;

		public TestWork(TestObject parent, TestObject child) {
			this.parent = parent;
			this.child = child;
		}

		public void task(TestObject parent) {
			assertSame("Incorrect parent", this.parent, parent);
			if (this.child != null) {
				assertSame("Incorrect child", this.child, parent.objectRegistry.getObject(0));
			}
		}
	}

}
