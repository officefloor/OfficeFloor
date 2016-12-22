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
package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFlowTypeImpl<F extends Enum<F>> implements
		ManagedObjectFlowType<F> {

	/**
	 * Name describing this flow.
	 */
	private final String name;

	/**
	 * Name of {@link Work} instigating the {@link Flow} or <code>null</code> if
	 * done directly by {@link ManagedObjectSource}.
	 */
	private final String workName;

	/**
	 * Name of {@link ManagedFunction} instigating the {@link Flow} or <code>null</code> if
	 * done directly by {@link ManagedObjectSource}.
	 */
	private final String taskName;

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
	 * Initiate for a {@link ManagedObjectFlowType} invoked from a {@link ManagedFunction}
	 * added by the {@link ManagedObjectSource}.
	 * 
	 * @param workName
	 *            Name of {@link Work} instigating the {@link Flow} or
	 *            <code>null</code> if done directly by
	 *            {@link ManagedObjectSource}.
	 * @param taskName
	 *            Name of {@link ManagedFunction} instigating the {@link Flow} or
	 *            <code>null</code> if done directly by
	 *            {@link ManagedObjectSource}.
	 * @param index
	 *            Index identifying this flow.
	 * @param argumentType
	 *            Type of argument given to this flow. May be <code>null</code>.
	 * @param key
	 *            Key identifying this flow. May be <code>null</code>.
	 * @param label
	 *            Label describing this flow. May be <code>null</code>.
	 */
	public ManagedObjectFlowTypeImpl(String workName, String taskName,
			int index, Class<?> argumentType, F key, String label) {
		this.workName = workName;
		this.taskName = taskName;
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

	/**
	 * Initiate for a {@link ManagedObjectFlowType} invoked directly by the
	 * {@link ManagedObjectSource}.
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
	public ManagedObjectFlowTypeImpl(int index, Class<?> argumentType, F key,
			String label) {
		this(null, null, index, argumentType, key, label);
	}

	/*
	 * ====================== ManagedObjectFlowType ============================
	 */

	@Override
	public String getFlowName() {
		return this.name;
	}

	@Override
	public String getWorkName() {
		return this.workName;
	}

	@Override
	public String getTaskName() {
		return this.taskName;
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