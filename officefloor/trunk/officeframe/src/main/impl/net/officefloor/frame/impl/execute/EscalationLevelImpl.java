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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.api.escalate.EscalationContext;
import net.officefloor.frame.api.escalate.EscalationPoint;
import net.officefloor.frame.internal.structure.EscalationLevel;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.EscalationLevel}.
 * 
 * @author Daniel
 */
public class EscalationLevelImpl<E extends Throwable>
		implements EscalationLevel<E> {

	/**
	 * Type of cause that this {@link EscalationLevel} covers.
	 */
	protected final Class<E> typeOfCause;

	/**
	 * {@link EscalationPoint} that handles the escalation.
	 */
	protected final EscalationPoint<E> escalationPoint;

	/**
	 * Initiate with details.
	 * 
	 * @param typeOfCause
	 *            Type of cause that this {@link EscalationLevel} covers.
	 * @param escalationPoint
	 *            {@link EscalationPoint} that handles the escalation.
	 */
	public EscalationLevelImpl(Class<E> typeOfCause,
			EscalationPoint<E> escalationPoint) {
		this.typeOfCause = typeOfCause;
		this.escalationPoint = escalationPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.EscalationLevel#getTypeOfCause()
	 */
	public Class<E> getTypeOfCause() {
		return this.typeOfCause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.EscalationLevel#escalate(net.officefloor.frame.api.escalate.EscalationContext)
	 */
	public void escalate(EscalationContext<E> context) {
		this.escalationPoint.escalate(context);
	}

}
