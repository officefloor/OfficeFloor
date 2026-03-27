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

package net.officefloor.frame.impl.execute.flow;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link FlowMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowMetaDataImpl implements FlowMetaData {

	/**
	 * {@link ManagedFunctionMetaData} of the initial {@link ManagedFunction} of
	 * the {@link Flow}.
	 */
	private final ManagedFunctionMetaData<?, ?> initialFunctionMetaData;

	/**
	 * Indicates whether the {@link Flow} should be instigated in a spawned
	 * {@link ThreadState}.
	 */
	private final boolean isSpawnThreadState;

	/**
	 * Initiate.
	 * 
	 * @param isSpawnThreadState
	 *            Indicates whether the {@link Flow} should be instigated in a
	 *            spawned {@link ThreadState}.
	 * @param initialFunctionMetaData
	 *            {@link ManagedFunctionMetaData} of the initial
	 *            {@link ManagedFunction} of the {@link Flow}.
	 */
	public FlowMetaDataImpl(boolean isSpawnThreadState, ManagedFunctionMetaData<?, ?> initialFunctionMetaData) {
		this.isSpawnThreadState = isSpawnThreadState;
		this.initialFunctionMetaData = initialFunctionMetaData;
	}

	/*
	 * =================== FlowMetaData ======================================
	 */

	@Override
	public ManagedFunctionMetaData<?, ?> getInitialFunctionMetaData() {
		return this.initialFunctionMetaData;
	}

	@Override
	public boolean isSpawnThreadState() {
		return this.isSpawnThreadState;
	}

}
