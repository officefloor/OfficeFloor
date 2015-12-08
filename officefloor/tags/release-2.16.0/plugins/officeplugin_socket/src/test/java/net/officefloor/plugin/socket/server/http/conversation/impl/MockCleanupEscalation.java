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
package net.officefloor.plugin.socket.server.http.conversation.impl;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.spi.managedobject.recycle.CleanupEscalation;

/**
 * Mock {@link CleanupEscalation}.
 *
 * @author Daniel Sagenschneider
 */
public class MockCleanupEscalation implements CleanupEscalation {

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * {@link Escalation}.
	 */
	private final Throwable escalation;

	/**
	 * Instantiate.
	 * 
	 * @param objectType
	 *            Object type.
	 * @param escalation
	 *            {@link Escalation}.
	 */
	public MockCleanupEscalation(Class<?> objectType, Throwable escalation) {
		this.objectType = objectType;
		this.escalation = escalation;
	}

	/*
	 * ===================== CleanupEscalation =============================
	 */

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public Throwable getEscalation() {
		return this.escalation;
	}

}