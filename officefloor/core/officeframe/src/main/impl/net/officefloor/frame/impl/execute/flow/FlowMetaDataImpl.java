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
