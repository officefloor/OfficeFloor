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
package net.officefloor.plugin.socket.server.http.protocol;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;

/**
 * Ensures appropriately cleans up {@link HttpManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CleanupTaskTest extends OfficeFrameTestCase {

	/**
	 * {@link CleanupTask}.
	 */
	private final CleanupTask task = new CleanupTask();

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<Work, None, None> context = this
			.createMock(TaskContext.class);

	/**
	 * {@link RecycleManagedObjectParameter}.
	 */
	@SuppressWarnings("unchecked")
	private final RecycleManagedObjectParameter<HttpManagedObject> parameter = this
			.createMock(RecycleManagedObjectParameter.class);

	/**
	 * {@link HttpManagedObject}.
	 */
	private final HttpManagedObject mo = this
			.createMock(HttpManagedObject.class);

	/**
	 * Ensure cleans up the {@link HttpManagedObject}.
	 */
	public void testCleanup() throws IOException {

		// Record cleaning up
		this.recordReturn(this.context, this.context.getObject(0),
				this.parameter);
		this.recordReturn(this.parameter, this.parameter.getManagedObject(),
				this.mo);
		this.mo.cleanup();

		// Test
		this.replayMockObjects();
		this.task.doTask(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure handles {@link ClosedChannelException} indicating the peer has
	 * closed the connection.
	 */
	public void testConnectionAlreadyClosed() throws IOException {

		// Record connection already closed
		this.recordReturn(this.context, this.context.getObject(0),
				this.parameter);
		this.recordReturn(this.parameter, this.parameter.getManagedObject(),
				this.mo);
		this.mo.cleanup();
		this.control(this.mo).setThrowable(new ClosedChannelException());

		// Test
		this.replayMockObjects();
		this.task.doTask(this.context);
		this.verifyMockObjects();
	}

}