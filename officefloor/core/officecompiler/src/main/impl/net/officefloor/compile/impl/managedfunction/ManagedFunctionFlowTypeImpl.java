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
package net.officefloor.compile.impl.managedfunction;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedFunctionFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionFlowTypeImpl<F extends Enum<F>>
		implements ManagedFunctionFlowType<F>, ManagedFunctionFlowTypeBuilder<F> {

	/**
	 * Index of this {@link ManagedFunctionFlowType}.
	 */
	private int index;

	/**
	 * Label for the {@link ManagedFunctionFlowType}.
	 */
	private String label = null;

	/**
	 * {@link Flow} key.
	 */
	private F key = null;

	/**
	 * Type of the argument.
	 */
	private Class<?> argumentType = null;

	/**
	 * Initiate.
	 * 
	 * @param index
	 *            Index of this {@link ManagedFunctionFlowType}.
	 */
	public ManagedFunctionFlowTypeImpl(int index) {
		this.index = index;
	}

	/*
	 * ==================== TaskFlowTypeBuilder ============================
	 */

	@Override
	public void setKey(F key) {
		this.key = key;
		this.index = key.ordinal();
	}

	@Override
	public void setArgumentType(Class<?> argumentType) {
		this.argumentType = argumentType;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * =================== TaskFlowType ===================================
	 */

	@Override
	public String getFlowName() {
		// Follow priorities to obtain the flow name
		if (!CompileUtil.isBlank(this.label)) {
			return this.label;
		} else if (this.key != null) {
			return this.key.toString();
		} else {
			return String.valueOf(this.index);
		}
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

	@Override
	public F getKey() {
		return this.key;
	}

}