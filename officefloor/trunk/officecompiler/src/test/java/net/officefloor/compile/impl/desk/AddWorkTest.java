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
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.model.desk.WorkModel;

/**
 * Tests the {@link DeskOperations}.
 * 
 * @author Daniel
 */
public class AddWorkTest extends AbstractDeskOperationsTestCase {

	/**
	 * Ensure can add {@link WorkModel}.
	 */
	public void testAddWork() throws Exception {

		// Create the work type to add
		WorkType<?> work = this.constructWorkType(new WorkTypeConstructor() {
			@Override
			public void construct(WorkTypeContext context) {
				TaskTypeConstructor task = context.addTask("TASK");
				task.addObject(Object.class, null);
				task.addFlow(String.class, null);
				task.addEscalation(Throwable.class);
			}
		});

		// Validate adding the work and reverting
		Change<WorkModel> change = this.operations.addWork("WORK", work);
		change.apply();
		this.validateDesk();
		change.revert();
		this.validateAsSetupDesk();
	}

}