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
package net.officefloor.compile.impl.governance;

import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.frame.api.execute.Task;

/**
 * {@link GovernanceFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceFlowTypeImpl<F extends Enum<F>> implements
		GovernanceFlowType<F> {

	/**
	 * Name describing this flow.
	 */
	private final String name;

	/**
	 * Index identifying this flow.
	 */
	private final int index;

	/**
	 * Type of argument given to this flow.
	 */
	private final Class<?> argumentType;

	/**
	 * Key identifying this flow.
	 */
	private final F key;

	/**
	 * Initiate for a {@link GovernanceFlowType} invoked from a {@link Task}
	 * added by the {@link GovernanceSource}.
	 * 
	 * @param index
	 *            Index identifying this flow.
	 * @param argumentType
	 *            Type of argument given to this flow. May be <code>null</code>.
	 * @param key
	 *            Key identifying this flow. May be <code>null</code>.
	 * @param label
	 *            Label describing this flow. May be <code>null</code>.
	 */
	public GovernanceFlowTypeImpl(int index, Class<?> argumentType, F key,
			String label) {
		this.index = index;
		this.key = key;

		// Ensure have argument type (default to void to indicate no argument)
		this.argumentType = (argumentType != null ? argumentType : Void.class);

		// Obtain the name for this flow
		if (!CompileUtil.isBlank(label)) {
			this.name = label;
		} else if (this.key != null) {
			this.name = this.key.toString();
		} else {
			this.name = String.valueOf(this.index);
		}
	}

	/*
	 * ====================== GovernanceFlowType ============================
	 */

	@Override
	public String getFlowName() {
		return this.name;
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