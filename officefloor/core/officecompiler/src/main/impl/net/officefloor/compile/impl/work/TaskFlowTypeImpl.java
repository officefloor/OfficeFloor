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
package net.officefloor.compile.impl.work;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link TaskFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskFlowTypeImpl<F extends Enum<F>> implements TaskFlowType<F>,
		TaskFlowTypeBuilder<F> {

	/**
	 * Index of this {@link TaskFlowType}.
	 */
	private int index;

	/**
	 * Label for the {@link TaskFlowType}.
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
	 *            Index of this {@link TaskFlowType}.
	 */
	public TaskFlowTypeImpl(int index) {
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