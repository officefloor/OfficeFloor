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

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.Work;
import net.officefloor.frame.api.source.TestSource;

/**
 * Mock {@link ManagedFunctionSource} for testing the {@link DeskModelSectionSource}.
 *
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockWorkSource extends AbstractManagedFunctionSource<Work> implements
		WorkFactory<Work>, Work, ManagedFunctionFactory<Work, Indexed, Indexed> {

	/*
	 * ================== WorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder<Work> workTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {
		workTypeBuilder.setWorkFactory(this);
		ManagedFunctionTypeBuilder<Indexed, Indexed> task = workTypeBuilder.addManagedFunctionType(
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
	public ManagedFunction<Work, Indexed, Indexed> createManagedFunction(Work work) {
		Assert.fail("Should not require creating task");
		return null;
	}

}