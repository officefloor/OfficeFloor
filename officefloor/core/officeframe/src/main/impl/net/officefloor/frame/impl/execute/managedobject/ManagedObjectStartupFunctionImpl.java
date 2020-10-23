/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectStartupFunction;

/**
 * {@link ManagedObjectStartupFunction} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectStartupFunctionImpl implements ManagedObjectStartupFunction {

	/**
	 * {@link FlowMetaData} for the startup {@link ManagedFunction}.
	 */
	private final FlowMetaData flowMetaData;

	/**
	 * Parameter to the {@link ManagedFunction}.
	 */
	private final Object parameter;

	/**
	 * Instantiate.
	 * 
	 * @param flowMetaData {@link FlowMetaData} for the startup
	 *                     {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 */
	public ManagedObjectStartupFunctionImpl(FlowMetaData flowMetaData, Object parameter) {
		this.flowMetaData = flowMetaData;
		this.parameter = parameter;
	}

	/*
	 * ====================== ManagedObjectStartupFunction =========================
	 */

	@Override
	public FlowMetaData getFlowMetaData() {
		return this.flowMetaData;
	}

	@Override
	public Object getParameter() {
		return this.parameter;
	}

}
