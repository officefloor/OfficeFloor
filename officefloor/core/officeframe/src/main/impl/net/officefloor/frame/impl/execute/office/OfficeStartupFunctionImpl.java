/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;

/**
 * {@link OfficeStartupFunction} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeStartupFunctionImpl implements OfficeStartupFunction {

	/**
	 * {@link FlowMetaData} for the {@link OfficeStartupFunction}.
	 */
	private final FlowMetaData flowMetaData;

	/**
	 * Parameter for the startup {@link Flow}.
	 */
	private final Object parameter;

	/**
	 * Initiate.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} for the {@link OfficeStartupFunction}.
	 * @param parameter
	 *            Parameter for the startup {@link Flow}.
	 */
	public OfficeStartupFunctionImpl(FlowMetaData flowMetaData, Object parameter) {
		this.flowMetaData = flowMetaData;
		this.parameter = parameter;
	}

	/*
	 * ================== OfficeStartupFunction ==================
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