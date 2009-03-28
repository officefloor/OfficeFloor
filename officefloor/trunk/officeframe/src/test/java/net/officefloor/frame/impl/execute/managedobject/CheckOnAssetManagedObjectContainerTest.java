/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.impl.execute.error.ExecutionError;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

import org.easymock.AbstractMatcher;

/**
 * Tests checking on the {@link ManagedObjectContainer} as an {@link Asset}.
 * 
 * @author Daniel
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
		this.record_MoContainer_init();
		this.record_MoUser_setFailure(false, failure);

		// Record failing job nodes on asset check
		this.check.failJobNodes(failure, true);

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
		this.record_MoContainer_init();
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
		this.record_MoContainer_init();

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
		this.record_MoContainer_init();

		// Record check that times out
		this.recordReturn(this.check, this.check.getTime(), this
				.getFutureTime(10000));
		this.record_Asset_checkOnAsset(10);
		this.check.failJobNodes(null, true);
		this.control(this.check).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertTrue("Incorrect failure",
						actual[0] instanceof ExecutionError);
				assertEquals("Failure should be permanent", true, actual[1]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		final ManagedObjectContainer mo = this.createManagedObjectContainer();
		((AsynchronousListener) mo).notifyStarted();
		((Asset) mo).checkOnAsset(this.check);
		this.verifyMockObjects();
	}

}