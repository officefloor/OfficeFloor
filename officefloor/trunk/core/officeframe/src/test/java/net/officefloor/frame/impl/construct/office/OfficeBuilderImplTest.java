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
package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link OfficeBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuilderImplTest extends OfficeFrameTestCase {

	/**
	 * {@link Office} name.
	 */
	private final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link OfficeBuilderImpl}.
	 */
	private OfficeBuilderImpl officeBuilder = new OfficeBuilderImpl(OFFICE_NAME);

	/**
	 * {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	private WorkFactory<Work> workFactory = this.createMock(WorkFactory.class);

	/**
	 * {@link TaskFactory}.
	 */
	@SuppressWarnings("unchecked")
	private TaskFactory<Work, Indexed, Indexed> taskFactory = this
			.createMock(TaskFactory.class);

	/**
	 * Ensure able to get the {@link FlowNodeBuilder}.
	 */
	public void testGetFlowNodeBuilder() {

		// Name spaced work name
		String namespacedWork = OfficeBuilderImpl.getNamespacedName(
				"namespace", "work");

		// Add a task
		TaskBuilder<Work, Indexed, Indexed> taskBuilder = this.officeBuilder
				.addWork(namespacedWork, this.workFactory).addTask("task",
						this.taskFactory);

		// Ensure can get task as flow node builder
		FlowNodeBuilder<?> flowNodeBuilder = this.officeBuilder
				.getFlowNodeBuilder("namespace", "work", "task");
		assertEquals("Incorrect flow node builder", taskBuilder,
				flowNodeBuilder);
	}

}