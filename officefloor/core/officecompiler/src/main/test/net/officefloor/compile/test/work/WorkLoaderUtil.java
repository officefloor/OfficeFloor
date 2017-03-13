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
package net.officefloor.compile.test.work;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.managedfunction.FunctionNamespaceTypeImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.function.Work;

/**
 * Utility class for testing a {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkLoaderUtil {

	/**
	 * Validates the {@link ManagedFunctionSourceSpecification} for the {@link ManagedFunctionSource}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param <WS>
	 *            {@link ManagedFunctionSource} type.
	 * @param workSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <W extends Work, WS extends ManagedFunctionSource<W>> PropertyList validateSpecification(
			Class<WS> workSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null)
				.getWorkLoader().loadSpecification(workSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList,
				propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link FunctionNamespaceBuilder} to create the expected
	 * {@link FunctionNamespaceType}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param workFactory
	 *            {@link WorkFactory} for the {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceBuilder} to build the expected {@link FunctionNamespaceType}.
	 */
	public static <W extends Work> FunctionNamespaceBuilder<W> createWorkTypeBuilder(
			WorkFactory<W> workFactory) {
		FunctionNamespaceBuilder<W> workTypeBuilder = new FunctionNamespaceTypeImpl<W>();
		workTypeBuilder.setWorkFactory(workFactory);
		return workTypeBuilder;
	}

	/**
	 * Convenience method that validates the {@link FunctionNamespaceType} loaded from the
	 * input {@link ManagedFunctionSource} against the expected {@link FunctionNamespaceType} from the
	 * {@link FunctionNamespaceBuilder}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param <WS>
	 *            {@link ManagedFunctionSource} type.
	 * @param expectedWorkType
	 *            {@link FunctionNamespaceBuilder} that has had the expected
	 *            {@link FunctionNamespaceType} built against it.
	 * @param workSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static <W extends Work, WS extends ManagedFunctionSource<W>> FunctionNamespaceType<W> validateWorkType(
			FunctionNamespaceBuilder<?> expectedWorkType, Class<WS> workSourceClass,
			String... propertyNameValues) {
		return validateWorkType(expectedWorkType, workSourceClass, null,
				propertyNameValues);
	}

	/**
	 * Convenience method that validates the {@link FunctionNamespaceType} loaded from the
	 * input {@link ManagedFunctionSource} against the expected {@link FunctionNamespaceType} from the
	 * {@link FunctionNamespaceBuilder}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param <WS>
	 *            {@link ManagedFunctionSource} type.
	 * @param expectedWorkType
	 *            {@link FunctionNamespaceBuilder} that has had the expected
	 *            {@link FunctionNamespaceType} built against it.
	 * @param workSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends Work, WS extends ManagedFunctionSource<W>> FunctionNamespaceType<W> validateWorkType(
			FunctionNamespaceBuilder<?> expectedWorkType, Class<WS> workSourceClass,
			OfficeFloorCompiler compiler, String... propertyNameValues) {

		// Cast to obtain expected work type
		if (!(expectedWorkType instanceof FunctionNamespaceType)) {
			TestCase.fail("expectedWorkType must be created from createWorkTypeBuilder");
		}
		FunctionNamespaceType<W> expectedWork = (FunctionNamespaceType<W>) expectedWorkType;

		// Load the actual work type
		FunctionNamespaceType<W> actualWork = loadWorkType(workSourceClass, compiler,
				propertyNameValues);
		TestCase.assertNotNull("Failed to load WorkType", actualWork);

		// Verify the work type
		TestCase.assertEquals("Incorrect work factory", expectedWork
				.getWorkFactory().getClass(), actualWork.getWorkFactory()
				.getClass());
		TestCase.assertEquals("Incorrect number of tasks",
				expectedWork.getManagedFunctionTypes().length,
				actualWork.getManagedFunctionTypes().length);
		for (int i = 0; i < expectedWork.getManagedFunctionTypes().length; i++) {
			ManagedFunctionType<W, ?, ?> expectedTask = expectedWork.getManagedFunctionTypes()[i];
			ManagedFunctionType<W, ?, ?> actualTask = actualWork.getManagedFunctionTypes()[i];

			// Verify the task type
			TestCase.assertEquals("Incorrect task name (task=" + i + ")",
					expectedTask.getFunctionName(), actualTask.getFunctionName());
			TestCase.assertEquals("Incorrect return type (task=" + i + ")",
					expectedTask.getReturnType(), actualTask.getReturnType());
			TestCase.assertEquals("Incorrect dependency keys (task="
					+ expectedTask.getFunctionName() + ")",
					expectedTask.getObjectKeyClass(),
					actualTask.getObjectKeyClass());
			TestCase.assertEquals(
					"Incorrect flow keys (task=" + expectedTask.getFunctionName()
							+ ")", expectedTask.getFlowKeyClass(),
					actualTask.getFlowKeyClass());

			// Verify differentiator
			Object expectedDifferentiator = expectedTask.getDifferentiator();
			Object actualDifferentiator = actualTask.getDifferentiator();
			if (expectedDifferentiator == null) {
				TestCase.assertNull("Should not have differentiator (task="
						+ expectedTask.getFunctionName() + ")",
						actualDifferentiator);
			} else {
				// Match differentiator on type
				TestCase.assertEquals("Incorrect differentiator type (task="
						+ expectedTask.getFunctionName() + ")",
						expectedDifferentiator.getClass(),
						(actualDifferentiator == null ? null
								: actualDifferentiator.getClass()));
			}

			// If work factory and task factory match then should be so
			if (expectedWork.getWorkFactory() == expectedTask.getManagedFunctionFactory()) {
				TestCase.assertTrue(
						"WorkFactory and TaskFactoryManufacturer should be the same",
						(actualWork.getWorkFactory() == actualTask
								.getManagedFunctionFactory()));
			}

			// Verify the dependencies
			TestCase.assertEquals("Incorrect number of dependences (task="
					+ expectedTask.getFunctionName() + ")",
					expectedTask.getObjectTypes().length,
					actualTask.getObjectTypes().length);
			for (int d = 0; d < expectedTask.getObjectTypes().length; d++) {
				ManagedFunctionObjectType<?> expectedDependency = expectedTask
						.getObjectTypes()[d];
				ManagedFunctionObjectType<?> actualDependency = actualTask
						.getObjectTypes()[d];

				// Verify the dependency
				TestCase.assertEquals("Incorrect dependency key (task="
						+ expectedTask.getFunctionName() + ", dependency=" + d
						+ ")", expectedDependency.getKey(),
						actualDependency.getKey());
				TestCase.assertEquals("Incorrect dependency type (task="
						+ expectedTask.getFunctionName() + ", dependency=" + d
						+ ")", expectedDependency.getObjectType(),
						actualDependency.getObjectType());
				TestCase.assertEquals("Incorrect dependency qualifier (task="
						+ expectedTask.getFunctionName() + ", dependency=" + d
						+ ")", expectedDependency.getTypeQualifier(),
						actualDependency.getTypeQualifier());
				TestCase.assertEquals("Incorrect dependency index (task="
						+ expectedTask.getFunctionName() + ", dependency=" + d
						+ ")", expectedDependency.getIndex(),
						actualDependency.getIndex());
				TestCase.assertEquals("Incorrect dependency name (task="
						+ expectedTask.getFunctionName() + ", dependency=" + d
						+ ")", expectedDependency.getObjectName(),
						actualDependency.getObjectName());
			}

			// Verify the flows
			TestCase.assertEquals("Incorrect number of flows (task="
					+ expectedTask.getFunctionName() + ")",
					expectedTask.getFlowTypes().length,
					actualTask.getFlowTypes().length);
			for (int f = 0; f < expectedTask.getFlowTypes().length; f++) {
				ManagedFunctionFlowType<?> expectedFlow = expectedTask.getFlowTypes()[f];
				ManagedFunctionFlowType<?> actualFlow = actualTask.getFlowTypes()[f];

				// Verify the flow
				TestCase.assertEquals("Incorrect flow key (task="
						+ expectedTask.getFunctionName() + ", flow=" + f + ")",
						expectedFlow.getKey(), actualFlow.getKey());
				TestCase.assertEquals("Incorrect flow argument type (task="
						+ expectedTask.getFunctionName() + ", flow=" + f + ")",
						expectedFlow.getArgumentType(),
						actualFlow.getArgumentType());
				TestCase.assertEquals("Incorrect flow index (task="
						+ expectedTask.getFunctionName() + ", flow=" + f + ")",
						expectedFlow.getIndex(), actualFlow.getIndex());
				TestCase.assertEquals("Incorrect flow name (task="
						+ expectedTask.getFunctionName() + ", flow=" + f + ")",
						expectedFlow.getFlowName(), actualFlow.getFlowName());
			}

			// Verify the escalations
			TestCase.assertEquals("Incorrect number of escalations (task=" + i
					+ ")", expectedTask.getEscalationTypes().length,
					actualTask.getEscalationTypes().length);
			for (int e = 0; e < expectedTask.getEscalationTypes().length; e++) {
				ManagedFunctionEscalationType expectedEscalation = expectedTask
						.getEscalationTypes()[e];
				ManagedFunctionEscalationType actualEscalation = actualTask
						.getEscalationTypes()[e];

				// Verify the flow
				TestCase.assertEquals("Incorrect escalation type (task="
						+ expectedTask.getFunctionName() + ", escalation=" + e
						+ ")", expectedEscalation.getEscalationType(),
						actualEscalation.getEscalationType());
				TestCase.assertEquals("Incorrect escalation name (task="
						+ expectedTask.getFunctionName() + ", escalation=" + e
						+ ")", expectedEscalation.getEscalationName(),
						actualEscalation.getEscalationName());
			}
		}

		// Return the actual work type
		return actualWork;
	}

	/**
	 * Convenience method that loads the {@link FunctionNamespaceType} by obtaining the
	 * {@link ClassLoader} from the {@link ManagedFunctionSource} class.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param <WS>
	 *            {@link ManagedFunctionSource} type.
	 * @param workSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static <W extends Work, WS extends ManagedFunctionSource<W>> FunctionNamespaceType<W> loadWorkType(
			Class<WS> workSourceClass, String... propertyNameValues) {
		// Return the loaded work
		return loadWorkType(workSourceClass, null, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link FunctionNamespaceType} with the provided
	 * {@link OfficeFloorCompiler}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param <WS>
	 *            {@link ManagedFunctionSource} type.
	 * @param workSourceClass
	 *            {@link ManagedFunctionSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link ManagedFunctionSource}.
	 * @return Loaded {@link FunctionNamespaceType}.
	 */
	public static <W extends Work, WS extends ManagedFunctionSource<W>> FunctionNamespaceType<W> loadWorkType(
			Class<WS> workSourceClass, OfficeFloorCompiler compiler,
			String... propertyNameValues) {

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Return the loaded work
		return getOfficeFloorCompiler(compiler).getWorkLoader().loadFunctionNamespaceType(
				workSourceClass, propertyList);
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler(
			OfficeFloorCompiler compiler) {
		if (compiler == null) {
			// Create the office floor compiler that fails on first issue
			compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
			compiler.setCompilerIssues(new FailTestCompilerIssues());
		}
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private WorkLoaderUtil() {
	}

}