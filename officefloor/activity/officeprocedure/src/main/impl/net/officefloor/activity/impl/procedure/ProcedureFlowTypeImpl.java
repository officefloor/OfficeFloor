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
package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ProcedureFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureFlowTypeImpl implements ProcedureFlowType {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Argument type.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param flowName     Name of the {@link Flow}.
	 * @param argumentType Argument type.
	 */
	public ProcedureFlowTypeImpl(String flowName, Class<?> argumentType) {
		this.flowName = flowName;
		this.argumentType = argumentType;
	}

	/*
	 * =================== ProcedureFlowType ===========================
	 */

	@Override
	public String getFlowName() {
		// TODO implement ProcedureFlowType.getFlowName
		throw new UnsupportedOperationException("TODO implement ProcedureFlowType.getFlowName");
	}

	@Override
	public Class<?> getArgumentType() {
		// TODO implement ProcedureFlowType.getArgumentType
		throw new UnsupportedOperationException("TODO implement ProcedureFlowType.getArgumentType");
	}

}