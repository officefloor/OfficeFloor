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
package net.officefloor.compile.test.work;

import java.util.List;

import junit.framework.TestCase;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.work.WorkLoaderImpl;
import net.officefloor.compile.impl.work.WorkTypeImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.TaskEscalationType;
import net.officefloor.compile.spi.work.TaskFlowType;
import net.officefloor.compile.spi.work.TaskObjectType;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkLoader;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.issues.FailCompilerIssues;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskWorkModel;

/**
 * Utility class for testing a {@link WorkSource}.
 * 
 * @author Daniel
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

		// Create the work loader
		WorkLoader workLoader = new WorkLoaderImpl("TEST", "TEST");

		// Load the specification
		PropertyList propertyList = workLoader.loadSpecification(
				workSourceClass, new FailCompilerIssues());

		// Verify the properties
		List<Property> properties = propertyList.getPropertyList();
		TestCase.assertEquals("Incorrect number of properties",
				propertyNameLabels.length / 2, properties.size());
		for (int i = 0; i < propertyNameLabels.length; i += 2) {
			Property property = properties.get(i / 2);
			String name = propertyNameLabels[i];
			String label = propertyNameLabels[i + 1];
			TestCase.assertEquals("Incorrect name for property " + i, name,
					property.getName());
			TestCase.assertEquals("Incorrect label for property " + i, label,
					property.getLabel());
		}

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
	 * input {@link WorkSource} against the {@link DeskWorkModel}.
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
	@SuppressWarnings("unchecked")
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> validateWorkType(
			WorkTypeBuilder<?> expectedWorkType, Class<WS> workSourceClass,
			String... propertyNameValues) {

		// Cast to obtain expected work type
		if (!(expectedWorkType instanceof WorkType)) {
			TestCase
					.fail("expectedWorkType must be created from createWorkTypeBuilder");
		}
		WorkType<W> expectedWork = (WorkType<W>) expectedWorkType;

		// Load the actual work type
		WorkType<W> actualWork = loadWork(workSourceClass, propertyNameValues);

		// Verify the work type
		TestCase.assertEquals("Incorrect work factory", expectedWork
				.getWorkFactory().getClass(), actualWork.getWorkFactory()
				.getClass());
		TestCase.assertEquals("Incorrect number of tasks", expectedWork
				.getTaskTypes().length, actualWork.getTaskTypes().length);
		for (int i = 0; i < expectedWork.getTaskTypes().length; i++) {
			TaskType<W, ?, ?> expectedTask = expectedWork.getTaskTypes()[i];
			TaskType<W, ?, ?> actualTask = actualWork.getTaskTypes()[i];

			// Verify the task type
			TestCase.assertEquals("Incorrect task name (task=" + i + ")",
					expectedTask.getTaskName(), actualTask.getTaskName());
			TestCase.assertEquals("Incorrect dependency keys (task=" + i + ")",
					expectedTask.getObjectKeyClass(), actualTask
							.getObjectKeyClass());
			TestCase.assertEquals("Incorrect flow keys (task=" + i + ")",
					expectedTask.getFlowKeyClass(), actualTask
							.getFlowKeyClass());

			// If work factory and task factory match then should be so
			if (expectedWork.getWorkFactory() == expectedTask
					.getTaskFactoryManufacturer()) {
				TestCase
						.assertTrue(
								"WorkFactory and TaskFactoryManufacturer should be the same",
								(actualWork.getWorkFactory() == actualTask
										.getTaskFactoryManufacturer()));
			}

			// Verify the dependencies
			TestCase.assertEquals("Incorrect number of dependences (task=" + i
					+ ")", expectedTask.getObjectTypes().length, actualTask
					.getObjectTypes().length);
			for (int d = 0; d < expectedTask.getObjectTypes().length; d++) {
				TaskObjectType<?> expectedDependency = expectedTask
						.getObjectTypes()[d];
				TaskObjectType<?> actualDependency = actualTask
						.getObjectTypes()[d];

				// Verify the dependency
				TestCase.assertEquals("Incorrect dependency key (task=" + i
						+ ", dependency=" + d + ")", expectedDependency
						.getKey(), actualDependency.getKey());
				TestCase.assertEquals("Incorrect dependency type (task=" + i
						+ ", dependency=" + d + ")", expectedDependency
						.getObjectType(), actualDependency.getObjectType());
				TestCase.assertEquals("Incorrect dependency index (task=" + i
						+ ", dependency=" + d + ")", expectedDependency
						.getIndex(), actualDependency.getIndex());
				TestCase.assertEquals("Incorrect dependency name (task=" + i
						+ ", dependency=" + d + ")", expectedDependency
						.getObjectName(), actualDependency.getObjectName());
			}

			// Verify the flows
			TestCase.assertEquals("Incorrect number of flows (task=" + i + ")",
					expectedTask.getFlowTypes().length, actualTask
							.getFlowTypes().length);
			for (int f = 0; f < expectedTask.getFlowTypes().length; f++) {
				TaskFlowType<?> expectedFlow = expectedTask.getFlowTypes()[f];
				TaskFlowType<?> actualFlow = actualTask.getFlowTypes()[f];

				// Verify the flow
				TestCase.assertEquals("Incorrect flow key (task=" + i
						+ ", flow=" + f + ")", expectedFlow.getKey(),
						actualFlow.getKey());
				TestCase.assertEquals("Incorrect flow argument type (task=" + i
						+ ", flow=" + f + ")", expectedFlow.getArgumentType(),
						actualFlow.getArgumentType());
				TestCase.assertEquals("Incorrect flow index (task=" + i
						+ ", flow=" + f + ")", expectedFlow.getIndex(),
						actualFlow.getIndex());
				TestCase.assertEquals("Incorrect flow name (task=" + i
						+ ", flow=" + f + ")", expectedFlow.getFlowName(),
						actualFlow.getFlowName());
			}

			// Verify the escalations
			TestCase.assertEquals("Incorrect number of escalations (task=" + i
					+ ")", expectedTask.getEscalationTypes().length, actualTask
					.getEscalationTypes().length);
			for (int e = 0; e < expectedTask.getEscalationTypes().length; e++) {
				TaskEscalationType expectedEscalation = expectedTask
						.getEscalationTypes()[e];
				TaskEscalationType actualEscalation = actualTask
						.getEscalationTypes()[e];

				// Verify the flow
				TestCase.assertEquals("Incorrect escalation type (task=" + i
						+ ", escalation=" + e + ")", expectedEscalation
						.getEscalationType(), actualEscalation
						.getEscalationType());
				TestCase.assertEquals("Incorrect escalation name (task=" + i
						+ ", escalation=" + e + ")", expectedEscalation
						.getEscalationName(), actualEscalation
						.getEscalationName());
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
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWork(
			Class<WS> workSourceClass, String... propertyNameValues) {

		// Obtain the class loader
		ClassLoader classLoader = workSourceClass.getClassLoader();

		// Return the loaded work
		return loadWork(workSourceClass, classLoader, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link WorkType}.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkSource}.
	 * @return Loaded {@link WorkType}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWork(
			Class<WS> workSourceClass, ClassLoader classLoader,
			String... propertyNameValues) {

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the work loader
		WorkLoader workLoader = new WorkLoaderImpl("TEST", "TEST");

		// Return the loaded work
		return workLoader.loadWorkType(workSourceClass, propertyList, classLoader,
				new FailCompilerIssues());
	}

	/**
	 * All access via static methods.
	 */
	private WorkLoaderUtil() {
	}

}