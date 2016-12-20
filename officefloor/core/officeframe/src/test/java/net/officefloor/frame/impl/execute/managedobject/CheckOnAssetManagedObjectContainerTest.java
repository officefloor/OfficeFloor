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
package net.officefloor.frame.impl.execute.managedobject;

import java.sql.Connection;

import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

import org.easymock.AbstractMatcher;

/**
 * Tests checking on the {@link ManagedObjectContainer} as an {@link Asset}.
 * 
 * @author Daniel Sagenschneider
 */
public class CheckOnAssetManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * {@link CheckAssetContext}.
	 */
	private final CheckAssetContext check = this
			.createMock(CheckAssetContext.class);

	/**
	 * Ensures indicates existing failure.
	 */
	public void testExistingFailure() {

		final Exception failure = new Exception("Existing failure");

		// Record create managed object container and specify failure
		this.record_MoContainer_init(Connection.class);
		this.record_MoUser_setFailure(false, failure);

		// Should fail immediately and not check
		this.check.failFunctions(null, true);
		this.control(this.check).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				FailedToSourceManagedObjectEscalation escalation = (FailedToSourceManagedObjectEscalation) actual[0];
				assertEquals("Incorrect failure", failure, escalation
						.getCause());
				assertEquals("Incorrect object type", Connection.class,
						escalation.getObjectType());
				assertEquals("Should be permanent failure", true, actual[1]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();

		// Create the managed object and load it with failure
		final ManagedObjectContainer mo = this.createManagedObjectContainer();
		((ManagedObjectUser) mo).setFailure(failure);

		// Run check that should fail job nodes with existing failure
		((Asset) mo).checkOnAsset(this.check);

		this.verifyMockObjects();
	}

	/**
	 * Ensures does nothing if not in an asynchronous operation.
	 */
	public void testNoAsynchronousOperation() {

		// Record create managed object container
		this.record_MoContainer_init(Object.class);
		// Check should do nothing

		// Test
		this.replayMockObjects();
		final ManagedObjectContainer mo = this.createManagedObjectContainer();
		((Asset) mo).checkOnAsset(this.check);
		this.verifyMockObjects();
	}

	/**
	 * Ensures does nothing if not yet timed out.
	 */
	public void testNotTimedOut() {

		final long notTimeoutTime = System.currentTimeMillis();

		// Record create and in asynchronous operation
		this.record_MoContainer_init(Object.class);

		// Record check that does not time out
		this.recordReturn(this.check, this.check.getTime(), notTimeoutTime);
		this.record_Asset_checkOnAsset(1000);

		// Test
		this.replayMockObjects();
		final ManagedObjectContainer mo = this.createManagedObjectContainer();
		((AsynchronousListener) mo).notifyStarted();
		((Asset) mo).checkOnAsset(this.check);
		this.verifyMockObjects();
	}

	/**
	 * Ensures does nothing if not yet timed out.
	 */
	public void testTimedOut() {

		// Record create and in asynchronous operation
		this.record_MoContainer_init(Connection.class);

		// Record check that times out
		this.recordReturn(this.check, this.check.getTime(), this
				.getFutureTime(10000));
		this.record_Asset_checkOnAsset(10);
		this.check.failFunctions(null, true);
		this.control(this.check).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertTrue(
						"Incorrect failure",
						actual[0] instanceof SourceManagedObjectTimedOutEscalation);
				assertEquals("Failure should be permanent", true, actual[1]);
				return true;
			}
		});
		this.record_setFailedState(SourceManagedObjectTimedOutEscalation.class,
				null);

		// Test
		this.replayMockObjects();
		final ManagedObjectContainer mo = this.createManagedObjectContainer();
		((AsynchronousListener) mo).notifyStarted();
		((Asset) mo).checkOnAsset(this.check);
		this.verifyMockObjects();
	}

}