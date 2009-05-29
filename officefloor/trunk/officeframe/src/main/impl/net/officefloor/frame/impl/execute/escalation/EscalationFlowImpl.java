/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.FlowMetaData;

/**
 * Implementation of {@link EscalationFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationFlowImpl implements EscalationFlow {

	/**
	 * Type of cause handled by this {@link EscalationFlow}.
	 */
	private final Class<? extends Throwable> typeOfCause;

	/**
	 * {@link FlowMetaData} determine the actions for this {@link EscalationFlow}.
	 */
	private final FlowMetaData<?> flowMetaData;

	/**
	 * Initiate.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param flowMetaData
	 *            {@link FlowMetaData} determine the actions for this
	 *            {@link EscalationFlow}.
	 */
	public EscalationFlowImpl(Class<? extends Throwable> typeOfCause,
			FlowMetaData<?> flowMetaData) {
		this.typeOfCause = typeOfCause;
		this.flowMetaData = flowMetaData;
	}

	/*
	 * ======================== Escalation ====================================
	 */
	
	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return this.typeOfCause;
	}

	@Override
	public FlowMetaData<?> getFlowMetaData() {
		return this.flowMetaData;
	}

}