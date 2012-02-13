/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Escalation levels for a {@link ThreadState}.
 * <p>
 * The order of {@link Escalation} handling is as listed below.
 * 
 * @author Daniel Sagenschneider
 */
public enum EscalationLevel {

	/**
	 * {@link ThreadState} executing the {@link Flow} instances with all
	 * {@link Escalation} instances handled by {@link Flow}.
	 */
	FLOW,

	/**
	 * {@link ThreadState} invoking {@link Escalation} provided by the
	 * {@link Office}.
	 */
	OFFICE,

	/**
	 * {@link ThreadState} invoking {@link EscalationHandler} provided by the
	 * {@link ManagedObjectSource} instigating a {@link Flow}.
	 */
	MANAGED_OBJECT_SOURCE_HANDLER,

	/**
	 * {@link ThreadState} invoking catch all {@link Escalation} provided by the
	 * {@link OfficeFloor}.
	 */
	OFFICE_FLOOR

}