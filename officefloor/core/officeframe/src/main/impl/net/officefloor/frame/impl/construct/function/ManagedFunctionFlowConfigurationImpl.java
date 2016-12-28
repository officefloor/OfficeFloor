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
package net.officefloor.frame.impl.construct.function;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.configuration.ManagedFunctionFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ManagedFunctionFlowConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionFlowConfigurationImpl<F extends Enum<F>> implements ManagedFunctionFlowConfiguration<F> {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Reference to the initial {@link ManagedFunction} of this {@link Flow}.
	 */
	private final ManagedFunctionReference functionReference;

	/**
	 * Indicates whether to spawn the {@link ThreadState}.
	 */
	private final boolean isSpawnThreadState;

	/**
	 * Index of the {@link Flow}.
	 */
	private final int index;

	/**
	 * Key of the {@link Flow}.
	 */
	private final F key;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of this {@link Flow}.
	 * @param functionReference
	 *            Reference to the initial {@link ManagedFunction} of this
	 *            {@link Flow}.
	 * @param isSpawnThreadState
	 *            Indicates whether to spawn the {@link ThreadState}.
	 * @param index
	 *            Index of this {@link Flow}.
	 * @param key
	 *            Key of the {@link Flow}.
	 */
	public ManagedFunctionFlowConfigurationImpl(String flowName, ManagedFunctionReference functionReference,
			boolean isSpawnThreadState, int index, F key) {
		this.flowName = flowName;
		this.functionReference = functionReference;
		this.isSpawnThreadState = isSpawnThreadState;
		this.index = index;
		this.key = key;
	}

	/*
	 * ======================= FlowConfiguration ==============================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public ManagedFunctionReference getInitialFunction() {
		return this.functionReference;
	}

	@Override
	public boolean isSpawnThreadState() {
		return this.isSpawnThreadState;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public F getKey() {
		return this.key;
	}

}