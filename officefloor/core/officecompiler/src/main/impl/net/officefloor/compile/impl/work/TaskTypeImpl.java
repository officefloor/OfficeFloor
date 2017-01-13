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
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedFunctionType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskTypeImpl<W extends Work, M extends Enum<M>, F extends Enum<F>>
		implements ManagedFunctionType<W, M, F>, ManagedFunctionTypeBuilder<M, F> {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String taskName;

	/**
	 * {@link ManagedFunctionFactory}.
	 */
	private final ManagedFunctionFactory<W, M, F> taskFactory;

	/**
	 * {@link Enum} providing keys for dependent {@link Object} instances.
	 */
	private final Class<M> objectKeyClass;

	/**
	 * {@link ManagedFunctionObjectType} instances.
	 */
	private final List<ManagedFunctionObjectType<M>> objects = new LinkedList<ManagedFunctionObjectType<M>>();

	/**
	 * {@link Enum} providing keys for instigated {@link Flow} instances.
	 */
	private final Class<F> flowKeyClass;

	/**
	 * {@link ManagedFunctionFlowType} instances.
	 */
	private final List<ManagedFunctionFlowType<F>> flows = new LinkedList<ManagedFunctionFlowType<F>>();

	/**
	 * {@link ManagedFunctionEscalationType} instances.
	 */
	private final List<ManagedFunctionEscalationType> escalations = new LinkedList<ManagedFunctionEscalationType>();

	/**
	 * Differentiator.
	 */
	private Object differentiator = null;

	/**
	 * Return type.
	 */
	private Class<?> returnType = null;

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of the {@link ManagedFunction}.
	 * @param taskFactory
	 *            {@link ManagedFunctionFactory}.
	 * @param objectKeyClass
	 *            {@link Enum} providing keys for dependent {@link Object}
	 *            instances.
	 * @param flowKeyClass
	 *            {@link Enum} providing keys for instigated {@link Flow}
	 *            instances.
	 */
	public TaskTypeImpl(String taskName, ManagedFunctionFactory<W, M, F> taskFactory,
			Class<M> objectKeyClass, Class<F> flowKeyClass) {
		this.taskName = taskName;
		this.taskFactory = taskFactory;
		this.objectKeyClass = objectKeyClass;
		this.flowKeyClass = flowKeyClass;
	}

	/*
	 * ==================== TaskTypeBuilder ===================================
	 */

	@Override
	public void setDifferentiator(Object differentiator) {
		this.differentiator = differentiator;
	}

	@Override
	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}

	@Override
	public ManagedFunctionObjectTypeBuilder<M> addObject(Class<?> objectType) {
		TaskObjectTypeImpl<M> object = new TaskObjectTypeImpl<M>(objectType,
				this.objects.size());
		this.objects.add(object);
		return object;
	}

	@Override
	public ManagedFunctionFlowTypeBuilder<F> addFlow() {
		TaskFlowTypeImpl<F> flow = new TaskFlowTypeImpl<F>(this.flows.size());
		this.flows.add(flow);
		return flow;
	}

	@Override
	public <E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(
			Class<E> escalationType) {
		TaskEscalationTypeImpl escalation = new TaskEscalationTypeImpl(
				escalationType);
		this.escalations.add(escalation);
		return escalation;
	}

	/*
	 * ========================== TaskType ================================
	 */

	@Override
	public String getFunctionName() {
		return this.taskName;
	}

	@Override
	public ManagedFunctionFactory<W, M, F> getManagedFunctionFactory() {
		return this.taskFactory;
	}

	@Override
	public Object getDifferentiator() {
		return this.differentiator;
	}

	@Override
	public Class<M> getObjectKeyClass() {
		return this.objectKeyClass;
	}

	@Override
	public ManagedFunctionObjectType<M>[] getObjectTypes() {
		ManagedFunctionObjectType<M>[] objectTypes = CompileUtil.toArray(this.objects,
				new ManagedFunctionObjectType[0]);
		Arrays.sort(objectTypes, new Comparator<ManagedFunctionObjectType<M>>() {
			@Override
			public int compare(ManagedFunctionObjectType<M> a, ManagedFunctionObjectType<M> b) {
				return a.getIndex() - b.getIndex();
			}
		});
		return objectTypes;
	}

	@Override
	public Class<F> getFlowKeyClass() {
		return this.flowKeyClass;
	}

	@Override
	public ManagedFunctionFlowType<F>[] getFlowTypes() {
		ManagedFunctionFlowType<F>[] flowTypes = CompileUtil.toArray(this.flows,
				new ManagedFunctionFlowType[0]);
		Arrays.sort(flowTypes, new Comparator<ManagedFunctionFlowType<F>>() {
			@Override
			public int compare(ManagedFunctionFlowType<F> a, ManagedFunctionFlowType<F> b) {
				return a.getIndex() - b.getIndex();
			}
		});
		return flowTypes;
	}

	@Override
	public ManagedFunctionEscalationType[] getEscalationTypes() {
		return this.escalations.toArray(new ManagedFunctionEscalationType[0]);
	}

	@Override
	public Class<?> getReturnType() {
		return this.returnType;
	}

}