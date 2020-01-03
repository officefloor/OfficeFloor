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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Meta-data of a {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowMetaData {

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the initial
	 * {@link ManagedFunction} within the {@link Flow}.
	 * 
	 * @return {@link ManagedFunctionMetaData} of the initial
	 *         {@link ManagedFunction} within the {@link Flow}.
	 */
	ManagedFunctionMetaData<?, ?> getInitialFunctionMetaData();

	/**
	 * Indicates whether the {@link Flow} should be instigated within a spawned
	 * {@link ThreadState}.
	 * 
	 * @return <code>true</code> to execute the {@link Flow} within a spawned
	 *         {@link ThreadState}.
	 */
	boolean isSpawnThreadState();

}
