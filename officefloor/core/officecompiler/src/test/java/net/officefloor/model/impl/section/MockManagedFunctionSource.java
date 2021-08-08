/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.model.impl.section;

import org.junit.Assert;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.TestSource;

/**
 * Mock {@link ManagedFunctionSource} for testing the
 * {@link SectionModelSectionSource}.
 *
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockManagedFunctionSource extends AbstractManagedFunctionSource
		implements ManagedFunctionFactory<Indexed, Indexed> {

	/*
	 * ================== ManagedFunctionSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceBuilder, ManagedFunctionSourceContext context)
			throws Exception {
		ManagedFunctionTypeBuilder<Indexed, Indexed> function = namespaceBuilder
				.addManagedFunctionType("MANAGED_FUNCTION", Indexed.class, Indexed.class);
		function.setFunctionFactory(this);
		function.addObject(Integer.class).setLabel("PARAMETER");
	}

	/*
	 * ================== ManagedFunctionFactory =========================
	 */

	@Override
	public ManagedFunction<Indexed, Indexed> createManagedFunction() {
		Assert.fail("Should not require creating function");
		return null;
	}

}
