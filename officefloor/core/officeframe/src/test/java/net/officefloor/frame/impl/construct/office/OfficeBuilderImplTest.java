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

import net.officefloor.frame.api.build.FunctionBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
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
	 * {@link ManagedFunctionFactory}.
	 */
	@SuppressWarnings("unchecked")
	private ManagedFunctionFactory<Indexed, Indexed> functionFactory = this.createMock(ManagedFunctionFactory.class);

	/**
	 * Ensure able to get the {@link FunctionBuilder}.
	 */
	public void testGetFlowNodeBuilder() {

		// Name spaced function name
		String namespacedFunction = OfficeBuilderImpl.getNamespacedName("namespace", "function");

		// Add a function
		ManagedFunctionBuilder<Indexed, Indexed> functionBuilder = this.officeBuilder
				.addManagedFunction(namespacedFunction, this.functionFactory);

		// Ensure can get function as flow node builder
		FunctionBuilder<?> flowNodeBuilder = this.officeBuilder.getFlowBuilder("namespace", "function");
		assertEquals("Incorrect flow node builder", functionBuilder, flowNodeBuilder);
	}

}