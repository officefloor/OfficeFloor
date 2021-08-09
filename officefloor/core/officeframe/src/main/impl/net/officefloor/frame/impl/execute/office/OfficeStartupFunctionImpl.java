/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
