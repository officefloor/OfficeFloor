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

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link FunctionNamespaceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkTypeImpl<W extends Work> implements FunctionNamespaceType<W>,
		FunctionNamespaceBuilder<W> {

	/**
	 * {@link WorkFactory}.
	 */
	private WorkFactory<W> workFactory;

	/**
	 * Listing of the {@link ManagedFunctionType} definitions.
	 */
	private final List<ManagedFunctionType<W, ?, ?>> tasks = new LinkedList<ManagedFunctionType<W, ?, ?>>();

	/*
	 * =================== WorkTypeBuilder ====================================
	 */

	@Override
	public void setWorkFactory(WorkFactory<W> workFactory) {
		this.workFactory = workFactory;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> addManagedFunctionType(
			String taskName, ManagedFunctionFactory<? super W, M, F> taskFactory,
			Class<M> objectKeysClass, Class<F> flowKeysClass) {
		TaskTypeImpl taskType = new TaskTypeImpl(taskName, taskFactory,
				objectKeysClass, flowKeysClass);
		this.tasks.add(taskType);
		return taskType;
	}

	/*
	 * =================== WorkType ===========================================
	 */

	@Override
	public WorkFactory<W> getWorkFactory() {
		return this.workFactory;
	}

	@Override
	public ManagedFunctionType<W, ?, ?>[] getManagedFunctionTypes() {
		// Create and return the sorted listing of task types
		ManagedFunctionType<W, ?, ?>[] taskTypes = CompileUtil.toArray(this.tasks,
				new ManagedFunctionType[0]);
		Arrays.sort(taskTypes, new Comparator<ManagedFunctionType<W, ?, ?>>() {
			@Override
			public int compare(ManagedFunctionType<W, ?, ?> a, ManagedFunctionType<W, ?, ?> b) {
				return a.getFunctionName().compareTo(b.getFunctionName());
			}
		});
		return taskTypes;
	}

}