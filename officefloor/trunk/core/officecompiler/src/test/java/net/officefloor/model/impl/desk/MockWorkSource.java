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
package net.officefloor.model.impl.desk;

import org.junit.Assert;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.TestSource;

/**
 * Mock {@link WorkSource} for testing the {@link DeskModelSectionSource}.
 *
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockWorkSource extends AbstractWorkSource<Work> implements
		WorkFactory<Work>, Work, TaskFactory<Work, Indexed, Indexed> {

	/*
	 * ================== WorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
			WorkSourceContext context) throws Exception {
		workTypeBuilder.setWorkFactory(this);
		TaskTypeBuilder<Indexed, Indexed> task = workTypeBuilder.addTaskType(
				"WORK_TASK", this, Indexed.class, Indexed.class);
		task.addObject(Integer.class).setLabel("PARAMETER");
	}

	/*
	 * ================== WorkFactory =========================
	 */

	@Override
	public Work createWork() {
		return this;
	}

	/*
	 * ================== TaskFactory =========================
	 */

	@Override
	public Task<Work, Indexed, Indexed> createTask(Work work) {
		Assert.fail("Should not require creating task");
		return null;
	}

}