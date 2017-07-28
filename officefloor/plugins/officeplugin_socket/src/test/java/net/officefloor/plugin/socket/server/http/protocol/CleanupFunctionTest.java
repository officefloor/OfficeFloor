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
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;

/**
 * Ensures appropriately cleans up {@link HttpManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CleanupFunctionTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpCleanupManagedFunction}.
	 */
	private final HttpCleanupManagedFunction task = new HttpCleanupManagedFunction();

	/**
	 * {@link ManagedFunctionContext}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedFunctionContext<None, None> context = this.createMock(ManagedFunctionContext.class);

	/**
	 * {@link RecycleManagedObjectParameter}.
	 */
	@SuppressWarnings("unchecked")
	private final RecycleManagedObjectParameter<HttpManagedObject> parameter = this
			.createMock(RecycleManagedObjectParameter.class);

	/**
	 * {@link HttpManagedObject}.
	 */
	private final HttpManagedObject mo = this.createMock(HttpManagedObject.class);

	/**
	 * Ensure cleans up the {@link HttpManagedObject}.
	 */
	public void testCleanup() throws IOException {

		final CleanupEscalation[] escalations = new CleanupEscalation[0];

		// Record cleaning up
		this.recordReturn(this.context, this.context.getObject(0), this.parameter);
		this.recordReturn(this.parameter, this.parameter.getManagedObject(), this.mo);
		this.recordReturn(this.parameter, this.parameter.getCleanupEscalations(), escalations);
		this.mo.cleanup(escalations);

		// Test
		this.replayMockObjects();
		this.task.execute(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure flags failure if previous cleanup {@link Escalation}.
	 */
	public void testPreviousCleanupEscalation() throws IOException {

		final CleanupEscalation[] escalations = new CleanupEscalation[] { this.createMock(CleanupEscalation.class) };

		// Record cleaning up
		this.recordReturn(this.context, this.context.getObject(0), this.parameter);
		this.recordReturn(this.parameter, this.parameter.getManagedObject(), this.mo);
		this.recordReturn(this.parameter, this.parameter.getCleanupEscalations(), escalations);
		this.mo.cleanup(escalations);

		// Test
		this.replayMockObjects();
		this.task.execute(this.context);
		this.verifyMockObjects();

	}

	/**
	 * Ensure handles {@link ClosedChannelException} indicating the peer has
	 * closed the connection.
	 */
	public void testConnectionAlreadyClosed() throws IOException {

		final CleanupEscalation[] escalations = new CleanupEscalation[0];

		// Record connection already closed
		this.recordReturn(this.context, this.context.getObject(0), this.parameter);
		this.recordReturn(this.parameter, this.parameter.getManagedObject(), this.mo);
		this.recordReturn(this.parameter, this.parameter.getCleanupEscalations(), escalations);
		this.mo.cleanup(escalations);
		this.control(this.mo).setThrowable(new ClosedChannelException());

		// Test
		this.replayMockObjects();
		this.task.execute(this.context);
		this.verifyMockObjects();
	}

}