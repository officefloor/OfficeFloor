/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.work;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.work.TaskEscalationType;
import net.officefloor.compile.spi.work.TaskFlowType;
import net.officefloor.compile.spi.work.TaskObjectType;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.source.TaskEscalationTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link TaskType} implementation.
 * 
 * @author Daniel
 */
public class TaskTypeImpl<W extends Work, M extends Enum<M>, F extends Enum<F>>
		implements TaskType<W, M, F>, TaskTypeBuilder<M, F> {

	/**
	 * Name of the {@link Task}.
	 */
	private final String taskName;

	/**
	 * {@link TaskFactoryManufacturer}.
	 */
	private final TaskFactoryManufacturer<W, M, F> taskFactoryManufacturer;

	/**
	 * {@link Enum} providing keys for dependent {@link Object} instances.
	 */
	private final Class<M> objectKeyClass;

	/**
	 * {@link TaskObjectType} instances.
	 */
	private final List<TaskObjectType<M>> objects = new LinkedList<TaskObjectType<M>>();

	/**
	 * {@link Enum} providing keys for instigated {@link Flow} instances.
	 */
	private final Class<F> flowKeyClass;

	/**
	 * {@link TaskFlowType} instances.
	 */
	private final List<TaskFlowType<F>> flows = new LinkedList<TaskFlowType<F>>();

	/**
	 * {@link TaskEscalationType} instances.
	 */
	private final List<TaskEscalationType> escalations = new LinkedList<TaskEscalationType>();

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param taskFactoryManufacturer
	 *            {@link TaskFactoryManufacturer}.
	 * @param objectKeyClass
	 *            {@link Enum} providing keys for dependent {@link Object}
	 *            instances.
	 * @param flowKeyClass
	 *            {@link Enum} providing keys for instigated {@link Flow}
	 *            instances.
	 */
	public TaskTypeImpl(String taskName,
			TaskFactoryManufacturer<W, M, F> taskFactoryManufacturer,
			Class<M> objectKeyClass, Class<F> flowKeyClass) {
		this.taskName = taskName;
		this.taskFactoryManufacturer = taskFactoryManufacturer;
		this.objectKeyClass = objectKeyClass;
		this.flowKeyClass = flowKeyClass;
	}

	/*
	 * ==================== TaskTypeBuilder ===================================
	 */

	@Override
	public TaskObjectTypeBuilder<M> addObject(Class<?> objectType) {
		TaskObjectTypeImpl<M> object = new TaskObjectTypeImpl<M>(objectType,
				this.objects.size());
		this.objects.add(object);
		return object;
	}

	@Override
	public TaskFlowTypeBuilder<F> addFlow() {
		TaskFlowTypeImpl<F> flow = new TaskFlowTypeImpl<F>(this.flows.size());
		this.flows.add(flow);
		return flow;
	}

	@Override
	public <E extends Throwable> TaskEscalationTypeBuilder addEscalation(
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
	public String getTaskName() {
		return this.taskName;
	}

	@Override
	public TaskFactoryManufacturer<W, M, F> getTaskFactoryManufacturer() {
		return this.taskFactoryManufacturer;
	}

	@Override
	public Class<M> getObjectKeyClass() {
		return this.objectKeyClass;
	}

	@Override
	public TaskObjectType<M>[] getObjectTypes() {
		TaskObjectType<M>[] objectTypes = CompileUtil.toArray(this.objects,
				new TaskObjectType[0]);
		Arrays.sort(objectTypes, new Comparator<TaskObjectType<M>>() {
			@Override
			public int compare(TaskObjectType<M> a, TaskObjectType<M> b) {
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
	public TaskFlowType<F>[] getFlowTypes() {
		TaskFlowType<F>[] flowTypes = CompileUtil.toArray(this.flows,
				new TaskFlowType[0]);
		Arrays.sort(flowTypes, new Comparator<TaskFlowType<F>>() {
			@Override
			public int compare(TaskFlowType<F> a, TaskFlowType<F> b) {
				return a.getIndex() - b.getIndex();
			}
		});
		return flowTypes;
	}

	@Override
	public TaskEscalationType[] getEscalationTypes() {
		return this.escalations.toArray(new TaskEscalationType[0]);
	}

}