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
import net.officefloor.model.impl.section.SectionModelSectionSource;

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
				.addManagedFunctionType("MANAGED_FUNCTION", this, Indexed.class, Indexed.class);
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