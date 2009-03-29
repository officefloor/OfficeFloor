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
package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.impl.execute.job.AbstractJobContainer;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * <p>
 * Propagates the {@link Escalation} to allow the {@link AbstractJobContainer}
 * to catch and handle it.
 * <p>
 * The {@link Escalation} (/{@link Throwable}) may be obtained from
 * {@link #getCause()}.
 * 
 * @author Daniel
 */
public class PropagateEscalationError extends Error {

	/**
	 * Initiate.
	 * 
	 * @param escalation
	 *            {@link Throwable} to be propagated to the
	 *            {@link AbstractJobContainer} to be handled. Is
	 *            {@link Throwable} as may be failures from a
	 *            {@link ManagedObjectSource} that requires to be handled.
	 */
	public PropagateEscalationError(Throwable escalation) {
		super(escalation);
	}

}