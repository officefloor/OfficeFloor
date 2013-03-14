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
package net.officefloor.plugin.web.http.template.section;

import java.lang.reflect.Array;

import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Iterates over the array objects sending them to the {@link HttpTemplate} for
 * rendering.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateArrayIteratorWorkSource
		extends
		AbstractWorkSource<HttpTemplateArrayIteratorWorkSource.HttpTemplateArrayIteratorTask> {

	/**
	 * Name of property for the array component type.
	 */
	public static final String PROPERTY_COMPONENT_TYPE_NAME = "component.type.name";

	/**
	 * Name of the {@link Task}.
	 */
	public static final String TASK_NAME = "iterate";

	/**
	 * Name of the {@link TaskObject} providing array.
	 */
	public static final String OBJECT_NAME = DependencyKeys.ARRAY.name();

	/**
	 * Name of the {@link TaskFlow} for rendering.
	 */
	public static final String FLOW_NAME = FlowKeys.RENDER_ELEMENT.name();

	/*
	 * ============================== WorkSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_COMPONENT_TYPE_NAME, "Component Type");
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpTemplateArrayIteratorTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the component type name
		String componentTypeName = context.getProperty("component.type.name");
		Class<?> componentType = context.loadClass(
				componentTypeName);
		Class<?> arrayType = Array.newInstance(componentType, 0).getClass();

		// Create the task
		HttpTemplateArrayIteratorTask task = new HttpTemplateArrayIteratorTask();

		// Specify work factory
		workTypeBuilder.setWorkFactory(task);

		// Specify the task
		TaskTypeBuilder<DependencyKeys, FlowKeys> taskBuilder = workTypeBuilder
				.addTaskType(TASK_NAME, task, DependencyKeys.class,
						FlowKeys.class);
		taskBuilder.addObject(arrayType).setKey(DependencyKeys.ARRAY);
		TaskFlowTypeBuilder<FlowKeys> flow = taskBuilder.addFlow();
		flow.setKey(FlowKeys.RENDER_ELEMENT);
		flow.setArgumentType(componentType);
	}

	/**
	 * {@link HttpTemplateArrayIteratorWorkSource} dependency keys.
	 */
	public static enum DependencyKeys {
		ARRAY
	}

	/**
	 * {@link HttpTemplateArrayIteratorWorkSource} flow keys.
	 */
	public static enum FlowKeys {
		RENDER_ELEMENT
	}

	/**
	 * {@link Task} implementation.
	 */
	public static class HttpTemplateArrayIteratorTask
			extends
			AbstractSingleTask<HttpTemplateArrayIteratorTask, DependencyKeys, FlowKeys> {

		/*
		 * ============================ Task ==================================
		 */

		@Override
		public Object doTask(
				TaskContext<HttpTemplateArrayIteratorTask, DependencyKeys, FlowKeys> context) {

			// Obtain the array
			Object[] array = (Object[]) context.getObject(DependencyKeys.ARRAY);
			if (array == null) {
				return null; // no array, no rendering
			}

			// Iterate over the array rendering the elements
			for (Object element : array) {
				context.doFlow(FlowKeys.RENDER_ELEMENT, element);
			}

			// No argument for next task
			return null;
		}
	}

}