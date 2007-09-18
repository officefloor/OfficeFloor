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
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ParentEscalationProcedure;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.EscalationProcedure}.
 * 
 * @author Daniel
 */
public class EscalationProcedureImpl implements EscalationProcedure,
		EscalationContext<Throwable> {

	/**
	 * Levels in order for escalation.
	 */
	protected final EscalationLevel<Throwable>[] escalationLevels;

	/**
	 * {@link ParentEscalationProcedure} to be taken if the provided
	 * {@link EscalationLevel} instances for this {@link EscalationProcedure} do
	 * not handle the escalation or they themselves cause escalations.
	 */
	protected final ParentEscalationProcedure parentEscalationProcedure;

	/**
	 * Initiate with escalation details.
	 * 
	 * @param escalationLevels
	 *            {@link EscalationLevel} instances in order to be taken.
	 * @param parentEscalationProcedure
	 *            {@link ParentEscalationProcedure} to be taken if the
	 *            {@link EscalationLevel} instances for this
	 *            {@link EscalationProcedure} do not handle the escalation or
	 *            they themselves cause escalations.
	 */
	public EscalationProcedureImpl(
			EscalationLevel<Throwable>[] escalationLevels,
			ParentEscalationProcedure parentEscalationProcedure) {
		// Store state
		this.escalationLevels = escalationLevels;
		this.parentEscalationProcedure = parentEscalationProcedure;
	}

	/*
	 * ====================================================================
	 * EscalationProcedure
	 * ====================================================================
	 */

	/**
	 * Cause of the escalation.
	 */
	protected Throwable cause;

	/**
	 * {@link ThreadState} of thread requiring the escalation.
	 */
	protected ThreadState threadState;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.EscalationProcedure#escalate(java.lang.Throwable,
	 *      net.officefloor.frame.internal.structure.ThreadState)
	 */
	public void escalate(Throwable cause, ThreadState threadState) {
		// Only one escalation per work at one time
		synchronized (threadState.getThreadLock()) {
			// Store state
			this.cause = cause;
			this.threadState = threadState;

			boolean isHandled = false;
			try {
				// Find escalation level to handle escalation
				int i = 0;
				while ((!isHandled) && (i++ < this.escalationLevels.length)) {

					// Obtain the current level
					EscalationLevel<Throwable> level = this.escalationLevels[i];

					// Check if this level will handle escalation
					if (level.getTypeOfCause().isInstance(cause)) {

						// This level will handle escalation
						level.escalate(this);

						// Escalation handled
						isHandled = true;
					}
				}
			} catch (Throwable ex) {
				// This escalation procedure caused further escalations

				// Set focus to new escalation to be handled
				this.cause = ex;
			}

			// Have parent procedure handle escalation if not handled
			if (!isHandled) {
				if (this.parentEscalationProcedure != null) {
					this.parentEscalationProcedure.escalate(this);
				} else {
					// No parent escalation therefore do best handling
					System.err.println(this.cause.getMessage());
					this.cause.printStackTrace(System.err);
				}
			}

			// Unset state as escalation complete
			this.cause = null;
			this.threadState = null;
		}
	}

	/*
	 * ====================================================================
	 * EscalationContext
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.escalate.EscalationContext#getException()
	 */
	public Throwable getException() {
		return this.cause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.escalate.EscalationContext#getThreadState()
	 */
	public ThreadState getThreadState() {
		return this.threadState;
	}

}
