/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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