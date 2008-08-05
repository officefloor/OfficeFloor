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

import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of {@link Escalation}.
 * 
 * @author Daniel
 */
public class EscalationImpl implements Escalation {

	/**
	 * Type of cause handled by this {@link Escalation}.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * Flag to indicate that {@link ThreadState} be reset before doing this
	 * {@link Escalation}.
	 */
	private final boolean isResetThreadState;

	/**
	 * {@link FlowMetaData} determine the actions for this {@link Escalation}.
	 */
	private final FlowMetaData<?> flowMetaData;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link Escalation}.
	 * @param isResetThreadState
	 *            Flag to indicate that {@link ThreadState} be reset before
	 *            doing this {@link Escalation}.
	 * @param flowMetaData
	 *            {@link FlowMetaData} determine the actions for this
	 *            {@link Escalation}.
	 */
	public EscalationImpl(Class<? extends Throwable> typeOfCause,
			boolean isResetThreadState, FlowMetaData<?> flowMetaData) {
		this.typeOfCause = typeOfCause;
		this.isResetThreadState = isResetThreadState;
		this.flowMetaData = flowMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Escalation#getTypeOfCause()
	 */
	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Escalation#isResetThreadState()
	 */
	@Override
	public boolean isResetThreadState() {
		return this.isResetThreadState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Escalation#getFlowMetaData()
	 */
	@Override
	public FlowMetaData<?> getFlowMetaData() {
		return this.flowMetaData;
	}

}
