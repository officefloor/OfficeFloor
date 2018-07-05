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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;

/**
 * Builds a {@link Flow} from a {@link ManagedFunctionContainer} or
 * {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface FlowBuilder<F extends Enum<F>> extends FunctionBuilder<F> {

	/**
	 * Specifies the next {@link ManagedFunction} to be executed.
	 * 
	 * @param functionName
	 *            Name of the next {@link ManagedFunction}.
	 * @param argumentType
	 *            Type of argument passed to the next {@link ManagedFunction}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void setNextFunction(String functionName, Class<?> argumentType);

}