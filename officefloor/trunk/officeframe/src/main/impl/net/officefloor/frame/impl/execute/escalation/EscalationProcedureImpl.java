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

import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;

/**
 * Implementation of the {@link EscalationProcedure}.
 * 
 * @author Daniel
 */
public class EscalationProcedureImpl implements EscalationProcedure {

	/**
	 * {@link EscalationFlow} instances in order for this procedure.
	 */
	private final EscalationFlow[] escalations;

	/**
	 * Initiate with {@link EscalationFlow} details.
	 * 
	 * @param escalations
	 *            {@link EscalationFlow} instances in order to be taken for this
	 *            procedure.
	 */
	public EscalationProcedureImpl(EscalationFlow... escalations) {
		this.escalations = escalations;
	}

	/*
	 * ============= EscalationProcedure ==================================
	 */

	@Override
	public EscalationFlow getEscalation(Throwable cause) {

		// Find the first matching escalation
		for (EscalationFlow escalation : this.escalations) {
			if (escalation.getTypeOfCause().isInstance(cause)) {
				// Use first matching
				return escalation;
			}
		}

		// Not found
		return null;
	}

}