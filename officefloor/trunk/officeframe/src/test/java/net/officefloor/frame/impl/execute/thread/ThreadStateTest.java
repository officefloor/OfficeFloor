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
package net.officefloor.frame.impl.execute.thread;

import org.easymock.internal.AlwaysMatcher;

import net.officefloor.frame.impl.execute.ThreadStateImpl;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ThreadState}.
 * 
 * @author Daniel
 */
public class ThreadStateTest extends OfficeFrameTestCase {

	/**
	 * {@link ThreadStateImpl} being tested.
	 */
	private ThreadStateImpl threadState;

	/**
	 * Mock {@link ProcessState}.
	 */
	private ProcessState processState = this.createMock(ProcessState.class);

	/**
	 * Mock {@link FlowMetaData}.
	 */
	private FlowMetaData<?> flowMetaData = this.createMock(FlowMetaData.class);

	/**
	 * Flow {@link AssetManager}.
	 */
	private AssetManager flowAssetManager = this.createMock(AssetManager.class);

	/**
	 * Thread {@link AssetMonitor}.
	 */
	private AssetMonitor threadMonitor = this.createMock(AssetMonitor.class);

	/**
	 * Ensures able to handle happy day scenario.
	 */
	public void testHappyDayScenario() {

		// Record creation
		this.recordThreadStateCreation();

		// Replay
		this.replayMockObjects();

		// Create
		this.createThreadState();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Records the creation of the {@link ThreadState}.
	 */
	private void recordThreadStateCreation() {
		this.recordReturn(this.flowMetaData,
				this.flowMetaData.getFlowManager(), this.flowAssetManager);
		this.recordReturn(this.flowAssetManager, this.flowAssetManager
				.createAssetMonitor(this.threadState, this.threadState),
				this.threadMonitor, new AlwaysMatcher());
	}

	/**
	 * Creates the {@link ThreadState}.
	 */
	private void createThreadState() {
		this.threadState = new ThreadStateImpl(this.processState,
				this.flowMetaData);
	}
}
