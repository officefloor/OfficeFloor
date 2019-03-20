/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.function;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;

/**
 * Context for the {@link Flow} instances from the {@link ManagedFunctionLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFlowContext<F extends Enum<F>> {

	/**
	 * Instigates a {@link Flow} to be run from the {@link ManagedFunctionLogic}.
	 * 
	 * @param key       Key identifying the {@link Flow} to instigate.
	 * @param parameter Parameter for the first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param callback  Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(F key, Object parameter, FlowCallback callback);

	/**
	 * <p>
	 * Similar to {@link #doFlow(Enum, Object, FlowCallback)} except that allows
	 * dynamic instigation of {@link Flow} instances.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * {@link Flow} instances available.
	 * 
	 * @param flowIndex Index identifying the {@link Flow} to instigate.
	 * @param parameter Parameter for the first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param callback  Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(int flowIndex, Object parameter, FlowCallback callback);

	/**
	 * Creates an {@link AsynchronousFlow} that must be completed before any further
	 * {@link Flow} is executed.
	 * 
	 * @return {@link AsynchronousFlow} that must be completed.
	 */
	AsynchronousFlow createAsynchronousFlow();

}