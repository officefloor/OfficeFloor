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
package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link AdministrationFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationFlowTypeImpl<F extends Enum<F>> implements AdministrationFlowType<F> {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Argument type to the {@link Flow}.
	 */
	private final Class<?> argumentType;

	/**
	 * Index identifying the {@link Flow}.
	 */
	private final int index;

	/**
	 * Key identifying the {@link Flow}.
	 */
	private final F key;

	/**
	 * Initiate.
	 * 
	 * @param flowName
	 *            Name of the {@link Flow}.
	 * @param argumentType
	 *            Argument type to the {@link Flow}.
	 * @param index
	 *            Index identifying the {@link Flow}.
	 * @param key
	 *            Key identifying the {@link Flow}.
	 */
	public AdministrationFlowTypeImpl(String flowName, Class<?> argumentType, int index, F key) {
		this.flowName = flowName;
		this.argumentType = argumentType;
		this.index = index;
		this.key = key;
	}

	/*
	 * ==================== AdministrationFlowType =======================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
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