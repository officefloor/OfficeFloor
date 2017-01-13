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
package net.officefloor.frame.api.administration;

import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context in which the {@link Administration} executes.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationContext<E extends Object, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the particular extensions.
	 * 
	 * @return Extension for the {@link ManagedObject} instances to be
	 *         administered.
	 */
	List<E> getExtensionInterfaces();

	/**
	 * Instigates a {@link Flow} to be run.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link ManagedFunction} of the
	 *            {@link Flow}.
	 * @param callback
	 *            {@link FlowCallback}.
	 */
	void doFlow(F key, Object parameter, FlowCallback callback);

	/**
	 * <p>
	 * Similar to {@link #doFlow(Enum, Object)} except that allows dynamic
	 * instigation of flows.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * {@link Flow} instances available.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link ManagedFunction} of the
	 *            {@link Flow}.
	 * @param callback
	 *            {@link FlowCallback}.
	 * 
	 * @see Indexed
	 */
	void doFlow(int flowIndex, Object parameter, FlowCallback callback);

	/**
	 * Obtains the {@link GovernanceManager} for the particular key.
	 * 
	 * @param key
	 *            Key identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(G key);

	/**
	 * Obtains the {@link GovernanceManager} for the index.
	 * 
	 * @param governanceIndex
	 *            Index identifying the {@link GovernanceManager}.
	 * @return {@link GovernanceManager}.
	 */
	GovernanceManager getGovernance(int governanceIndex);

}