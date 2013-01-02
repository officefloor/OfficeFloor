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
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.work.WorkTypeImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * Utility class for testing a {@link WorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkLoaderUtil {

	/**
	 * Validates the {@link WorkSourceSpecification} for the {@link WorkSource}.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> PropertyList validateSpecification(
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
	 * Creates the {@link WorkTypeBuilder} to create the expected
	 * {@link WorkType}.
	 * 
	 * @param workFactory
	 *            {@link WorkFactory} for the {@link WorkType}.
	 * @return {@link WorkTypeBuilder} to build the expected {@link WorkType}.
	 */
	public static <W extends Work> WorkTypeBuilder<W> createWorkTypeBuilder(
			WorkFactory<W> workFactory) {
		WorkTypeBuilder<W> workTypeBuilder = new WorkTypeImpl<W>();
		workTypeBuilder.setWorkFactory(workFactory);
		return workTypeBuilder;
	}

	/**
	 * Convenience method that validates the {@link WorkType} loaded from the
	 * input {@link WorkSource} against the expected {@link WorkType} from the
	 * {@link WorkTypeBuilder}.
	 * 
	 * @param expectedWorkType
	 *            {@link WorkTypeBuilder} that has had the expected
	 *            {@link WorkType} built against it.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkSource}.
	 * @return Loaded {@link WorkType}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> validateWorkType(
			WorkTypeBuilder<?> expectedWorkType, Class<WS> workSourceClass,
			String... propertyNameValues) {
		return validateWorkType(expectedWorkType, workSourceClass, null,
				propertyNameValues);
	}

	/**
	 * Convenience method that validates the {@link WorkType} loaded from the
	 * input {@link WorkSource} against the expected {@link WorkType} from the
	 * {@link WorkTypeBuilder}.
	 * 
	 * @param expectedWorkType
	 *            {@link WorkTypeBuilder} that has had the expected
	 *            {@link WorkType} built against it.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkSource}.
	 * @return Loaded {@link WorkType}.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> validateWorkType(
			WorkTypeBuilder<?> expectedWorkType, Class<WS> workSourceClass,
			OfficeFloorCompiler compiler, String... propertyNameValues) {

		// Cast to obtain expected work type
		if (!(expectedWorkType instanceof WorkType)) {
			TestCase.fail("expectedWorkType must be created from createWorkTypeBuilder");
		}
		WorkType<W> expectedWork = (WorkType<W>) expectedWorkType;

		// Load the actual work type
		WorkType<W> actualWork = loadWorkType(workSourceClass, compiler,
				propertyNameValues);
		TestCase.assertNotNull("Failed to load WorkType", actualWork);

		// Verify the work type
		TestCase.assertEquals("Incorrect work factory", expectedWork
				.getWorkFactory().getClass(), actualWork.getWorkFactory()
				.getClass());
		TestCase.assertEquals("Incorrect number of tasks",
				expectedWork.getTaskTypes().length,
				actualWork.getTaskTypes().length);
		for (int i = 0; i < expectedWork.getTaskTypes().length; i++) {
			TaskType<W, ?, ?> expectedTask = expectedWork.getTaskTypes()[i];
			TaskType<W, ?, ?> actualTask = actualWork.getTaskTypes()[i];

			// Verify the task type
			TestCase.assertEquals("Incorrect task name (task=" + i + ")",
					expectedTask.getTaskName(), actualTask.getTaskName());
			TestCase.assertEquals("Incorrect return type (task=" + i + ")",
					expectedTask.getReturnType(), actualTask.getReturnType());
			TestCase.assertEquals("Incorrect dependency keys (task="
					+ expectedTask.getTaskName() + ")",
					expectedTask.getObjectKeyClass(),
					actualTask.getObjectKeyClass());
			TestCase.assertEquals(
					"Incorrect flow keys (task=" + expectedTask.getTaskName()
							+ ")", expectedTask.getFlowKeyClass(),
					actualTask.getFlowKeyClass());

			// Verify differentiator
			Object expectedDifferentiator = expectedTask.getDifferentiator();
			Object actualDifferentiator = actualTask.getDifferentiator();
			if (expectedDifferentiator == null) {
				TestCase.assertNull("Should not have differentiator (task="
						+ expectedTask.getTaskName() + ")",
						actualDifferentiator);
			} else {
				// Match differentiator on type
				TestCase.assertEquals("Incorrect differentiator type (task="
						+ expectedTask.getTaskName() + ")",
						expectedDifferentiator.getClass(),
						(actualDifferentiator == null ? null
								: actualDifferentiator.getClass()));
			}

			// If work factory and task factory match then should be so
			if (expectedWork.getWorkFactory() == expectedTask.getTaskFactory()) {
				TestCase.assertTrue(
						"WorkFactory and TaskFactoryManufacturer should be the same",
						(actualWork.getWorkFactory() == actualTask
								.getTaskFactory()));
			}

			// Verify the dependencies
			TestCase.assertEquals("Incorrect number of dependences (task="
					+ expectedTask.getTaskName() + ")",
					expectedTask.getObjectTypes().length,
					actualTask.getObjectTypes().length);
			for (int d = 0; d < expectedTask.getObjectTypes().length; d++) {
				TaskObjectType<?> expectedDependency = expectedTask
						.getObjectTypes()[d];
				TaskObjectType<?> actualDependency = actualTask
						.getObjectTypes()[d];

				// Verify the dependency
				TestCase.assertEquals("Incorrect dependency key (task="
						+ expectedTask.getTaskName() + ", dependency=" + d
						+ ")", expectedDependency.getKey(),
						actualDependency.getKey());
				TestCase.assertEquals("Incorrect dependency type (task="
						+ expectedTask.getTaskName() + ", dependency=" + d
						+ ")", expectedDependency.getObjectType(),
						actualDependency.getObjectType());
				TestCase.assertEquals("Incorrect dependency qualifier (task="
						+ expectedTask.getTaskName() + ", dependency=" + d
						+ ")", expectedDependency.getTypeQualifier(),
						actualDependency.getTypeQualifier());
				TestCase.assertEquals("Incorrect dependency index (task="
						+ expectedTask.getTaskName() + ", dependency=" + d
						+ ")", expectedDependency.getIndex(),
						actualDependency.getIndex());
				TestCase.assertEquals("Incorrect dependency name (task="
						+ expectedTask.getTaskName() + ", dependency=" + d
						+ ")", expectedDependency.getObjectName(),
						actualDependency.getObjectName());
			}

			// Verify the flows
			TestCase.assertEquals("Incorrect number of flows (task="
					+ expectedTask.getTaskName() + ")",
					expectedTask.getFlowTypes().length,
					actualTask.getFlowTypes().length);
			for (int f = 0; f < expectedTask.getFlowTypes().length; f++) {
				TaskFlowType<?> expectedFlow = expectedTask.getFlowTypes()[f];
				TaskFlowType<?> actualFlow = actualTask.getFlowTypes()[f];

				// Verify the flow
				TestCase.assertEquals("Incorrect flow key (task="
						+ expectedTask.getTaskName() + ", flow=" + f + ")",
						expectedFlow.getKey(), actualFlow.getKey());
				TestCase.assertEquals("Incorrect flow argument type (task="
						+ expectedTask.getTaskName() + ", flow=" + f + ")",
						expectedFlow.getArgumentType(),
						actualFlow.getArgumentType());
				TestCase.assertEquals("Incorrect flow index (task="
						+ expectedTask.getTaskName() + ", flow=" + f + ")",
						expectedFlow.getIndex(), actualFlow.getIndex());
				TestCase.assertEquals("Incorrect flow name (task="
						+ expectedTask.getTaskName() + ", flow=" + f + ")",
						expectedFlow.getFlowName(), actualFlow.getFlowName());
			}

			// Verify the escalations
			TestCase.assertEquals("Incorrect number of escalations (task=" + i
					+ ")", expectedTask.getEscalationTypes().length,
					actualTask.getEscalationTypes().length);
			for (int e = 0; e < expectedTask.getEscalationTypes().length; e++) {
				TaskEscalationType expectedEscalation = expectedTask
						.getEscalationTypes()[e];
				TaskEscalationType actualEscalation = actualTask
						.getEscalationTypes()[e];

				// Verify the flow
				TestCase.assertEquals("Incorrect escalation type (task="
						+ expectedTask.getTaskName() + ", escalation=" + e
						+ ")", expectedEscalation.getEscalationType(),
						actualEscalation.getEscalationType());
				TestCase.assertEquals("Incorrect escalation name (task="
						+ expectedTask.getTaskName() + ", escalation=" + e
						+ ")", expectedEscalation.getEscalationName(),
						actualEscalation.getEscalationName());
			}
		}

		// Return the actual work type
		return actualWork;
	}

	/**
	 * Convenience method that loads the {@link WorkType} by obtaining the
	 * {@link ClassLoader} from the {@link WorkSource} class.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkSource}.
	 * @return Loaded {@link WorkType}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWorkType(
			Class<WS> workSourceClass, String... propertyNameValues) {
		// Return the loaded work
		return loadWorkType(workSourceClass, null, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link WorkType} with the provided
	 * {@link OfficeFloorCompiler}.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkSource}.
	 * @return Loaded {@link WorkType}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWorkType(
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
		return getOfficeFloorCompiler(compiler).getWorkLoader().loadWorkType(
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