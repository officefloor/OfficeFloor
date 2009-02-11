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

import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * Implementation of the {@link EscalationProcedure}.
 * 
 * @author Daniel
 */
public class EscalationProcedureImpl implements EscalationProcedure {

	/**
	 * Parent {@link EscalationProcedure} to be taken if the provided
	 * {@link Escalation} instances for this {@link EscalationProcedure} do not
	 * handle the escalation.
	 */
	protected final EscalationProcedure parentEscalationProcedure;

	/**
	 * {@link Escalation} instances in order for this procedure.
	 */
	protected final Escalation[] escalations;

	/**
	 * Initiate with {@link Escalation} details.
	 * 
	 * @param parentEscalationProcedure
	 *            {@link EscalationProcedure} to be taken if the
	 *            {@link Escalation} instances for this
	 *            {@link EscalationProcedure} do not handle the escalation.
	 * @param escalations
	 *            {@link Escalation} instances in order to be taken for this
	 *            procedure.
	 */
	public EscalationProcedureImpl(
			EscalationProcedure parentEscalationProcedure,
			Escalation... escalations) {
		this.parentEscalationProcedure = parentEscalationProcedure;
		this.escalations = escalations;
	}

	/**
	 * Initiate top level {@link EscalationProcedure}.
	 */
	public EscalationProcedureImpl() {
		// No further escalations
		this.parentEscalationProcedure = null;
		this.escalations = new Escalation[0];
	}

	/*
	 * ============= EscalationProcedure ==================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.EscalationProcedure#getEscalation
	 * (java.lang.Throwable)
	 */
	@Override
	public Escalation getEscalation(Throwable cause) {

		// Find the first matching escalation
		for (Escalation escalation : this.escalations) {
			if (escalation.getTypeOfCause().isInstance(cause)) {
				// Use first matching
				return escalation;
			}
		}

		// Not found so ask parent for escalation (if have one)
		return (this.parentEscalationProcedure == null ? null
				: this.parentEscalationProcedure.getEscalation(cause));
	}

}
