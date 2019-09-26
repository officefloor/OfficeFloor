/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * Creates the {@link Var} for the {@link MethodFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionVariableParameterFactory extends ManagedFunctionObjectParameterFactory {

	/**
	 * Instantiate.
	 * 
	 * @param objectIndex Object index.
	 */
	public ManagedFunctionVariableParameterFactory(int objectIndex) {
		super(objectIndex);
	}

	/*
	 * ================== ParameterFactory ====================================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) {
		return VariableManagedObjectSource.var(context.getObject(this.objectIndex));
	}

}