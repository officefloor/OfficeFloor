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
package net.officefloor.compile.impl.desk;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.desk.DeskOperations;
import net.officefloor.compile.impl.work.WorkTypeImpl;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.TaskEscalationTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskObjectTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkModel;

/**
 * Tests the {@link DeskOperations}.
 * 
 * @author Daniel
 */
public class DeskOperationsTest extends OfficeFrameTestCase {

	/**
	 * {@link DeskModel} having operations applied.
	 */
	private final DeskModel desk = new DeskModel();

	/**
	 * {@link DeskOperations} being tested.
	 */
	private final DeskOperations operations = new DeskOperationsImpl();

	/**
	 * Ensure can add {@link WorkModel}.
	 */
	public void testAddWork() {

		WorkTypeImpl<Work> work = this.createWorkType();

		Change<WorkModel> change = this.operations.addWork("WORK", work);
		change.apply();

		change.revert();
		
	}

	/**
	 * {@link WorkFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkFactory<Work> workFactory = this
			.createMock(WorkFactory.class);

	/**
	 * Creates the {@link WorkTypeImpl} to build and use a {@link WorkType}.
	 * 
	 * @return {@link WorkTypeImpl}.
	 */
	private WorkTypeImpl<Work> createWorkType() {
		WorkTypeImpl<Work> workTypeBuilder = new WorkTypeImpl<Work>();
		workTypeBuilder.setWorkFactory(this.workFactory);
		TaskTypeBuilder<?, ?> task;

		return workTypeBuilder;
	}

	private interface WorkTypeConstructor {
		void construct(WorkTypeContext context);
	}

	private interface WorkTypeContext {
		TaskTypeConstructor<Indexed, Indexed> addTask(String taskName);

		<D extends Enum<D>, F extends Enum<F>> TaskTypeConstructor<D, F> addTask(
				String taskName, Class<D> dependencyKeys, Class<F> flowKeys);
	}
	
	private interface TaskTypeConstructor<D extends Enum<D>, F extends Enum<F>> {
		TaskObjectTypeBuilder<D> addObject();

		TaskFlowTypeBuilder<F> addFlow();

		TaskEscalationTypeBuilder addEscalation();
	}


}