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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Handler for the completion of the {@link Flow}.
 *
 * @author Daniel Sagenschneider
 */
public interface FlowCompletion extends LinkedListSetEntry<FlowCompletion, ManagedFunctionContainer> {

	/**
	 * Obtains the {@link FunctionState} to notify completion of the {@link Flow}.
	 * 
	 * @param escalation Possible {@link Escalation} from the {@link Flow}. Will be
	 *                   <code>null</code> if {@link Flow} completed without
	 *                   {@link Escalation}.
	 * @return {@link FunctionState} to notify completion of the {@link Flow}.
	 */
	FunctionState flowComplete(Throwable escalation);

}