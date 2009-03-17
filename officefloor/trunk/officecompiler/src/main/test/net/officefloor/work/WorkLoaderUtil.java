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
package net.officefloor.work;

import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.compile.impl.work.source.WorkLoaderContextImpl;
import net.officefloor.compile.spi.work.source.WorkLoader;
import net.officefloor.compile.spi.work.source.WorkLoaderContext;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.model.task.TaskFactoryManufacturer;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;

/**
 * Utility class for testing a {@link WorkLoader}.
 * 
 * @author Daniel
 */
public class WorkLoaderUtil {

	/**
	 * Asserts the {@link WorkModel} instances match.
	 * 
	 * @param expected
	 *            Expected {@link WorkModel}.
	 * @param actual
	 *            Actual {@link WorkModel}.
	 * @see #assertWorkModelMatch(String, WorkModel, WorkModel)
	 */
	public static void assertWorkModelMatch(WorkModel<?> expected,
			WorkModel<?> actual) {
		assertWorkModelMatch("", expected, actual);
	}

	/**
	 * <p>
	 * Asserts the {@link WorkModel} instances match.
	 * <p>
	 * Note that this is not a complete equals as items such as
	 * {@link WorkFactory} and {@link TaskFactoryManufacturer} are only checked
	 * to be of same types.
	 * 
	 * @param messagePrefix
	 *            Prefix given to assertion messages.
	 * @param expected
	 *            Expected {@link WorkModel}.
	 * @param actual
	 *            Actual {@link WorkModel}.
	 */
	public static void assertWorkModelMatch(String messagePrefix,
			WorkModel<?> expected, WorkModel<?> actual) {

		// Provide smaller names to make code easier to read
		final String p = messagePrefix;
		final WorkModel<?> ew = expected;
		final WorkModel<?> aw = actual;

		// Validate work
		TestCase.assertEquals(p + "incorrect type of work", ew.getTypeOfWork(),
				aw.getTypeOfWork());

		// Allow for expected not to have work factory
		WorkFactory<?> expectedWorkFactory = ew.getWorkFactory();
		if (expectedWorkFactory != null) {
			TestCase.assertEquals(p + "incorrect work factory type", ew
					.getWorkFactory().getClass(), aw.getWorkFactory()
					.getClass());
		} else {
			TestCase.assertNotNull(p + "must have work factory", aw
					.getWorkFactory());
		}

		// Validate the tasks
		TestCase.assertEquals(p + "incorrect number of tasks", ew.getTasks()
				.size(), aw.getTasks().size());
		for (int t = 0; t < ew.getTasks().size(); t++) {
			TaskModel<?, ?> et = ew.getTasks().get(t);
			TaskModel<?, ?> at = aw.getTasks().get(t);

			// Validate task
			TestCase.assertEquals(p + "incorrect task name - task " + t, et
					.getTaskName(), at.getTaskName());
			TestCase.assertEquals(p + "incorrect flow keys class - task " + t,
					et.getFlowKeys(), at.getFlowKeys());
			TestCase.assertEquals(p
					+ " incorrect managed object keys class - task " + t, et
					.getManagedObjectKeys(), at.getManagedObjectKeys());

			// Allow for expected not to have task factory manufacturer
			TaskFactoryManufacturer expectedManufacturer = et
					.getTaskFactoryManufacturer();
			if (expectedManufacturer != null) {
				TestCase.assertEquals(p
						+ "incorrect task factory manufacturer type - task "
						+ t, et.getTaskFactoryManufacturer().getClass(), at
						.getTaskFactoryManufacturer().getClass());
			} else {
				TestCase.assertEquals(p
						+ "must have task factory manufacturer - task " + t, at
						.getTaskFactoryManufacturer());
			}

			// Validate the objects
			TestCase.assertEquals(
					p + "incorrect number of objects - task " + t, et
							.getObjects().size(), at.getObjects().size());
			for (int o = 0; o < et.getObjects().size(); o++) {
				TaskObjectModel<?> eo = et.getObjects().get(o);
				TaskObjectModel<?> ao = at.getObjects().get(o);

				// Validate object
				TestCase.assertEquals(p
						+ "incorrect managed object key - task " + t
						+ ", object " + o, eo.getManagedObjectKey(), ao
						.getManagedObjectKey());
				TestCase.assertEquals(p + "incorrect object type - task " + t
						+ ", object " + o, eo.getObjectType(), ao
						.getObjectType());
			}

			// Validate the flows
			TestCase.assertEquals(p + "incorrect number of flows - task " + t,
					et.getFlows().size(), at.getFlows().size());
			for (int f = 0; f < et.getFlows().size(); f++) {
				TaskFlowModel<?> ef = et.getFlows().get(f);
				TaskFlowModel<?> af = at.getFlows().get(f);

				// Validate flow
				TestCase.assertEquals(p + "incorrect flow label - task " + t
						+ ", flow " + f, ef.getLabel(), af.getLabel());
				TestCase.assertEquals(p + "incorrect flow index - task " + t
						+ ", flow " + f, ef.getFlowIndex(), af.getFlowIndex());
				TestCase.assertEquals(p + "incorrect flow key - task " + t
						+ ", flow " + f, ef.getFlowKey(), af.getFlowKey());
			}

			// Validate the escalations
			TestCase
					.assertEquals(p + "incorrect number of escalations - task "
							+ t, et.getEscalations().size(), at
							.getEscalations().size());
			for (int e = 0; e < et.getEscalations().size(); e++) {
				TaskEscalationModel ee = et.getEscalations().get(e);
				TaskEscalationModel ae = at.getEscalations().get(e);

				// Validate escalation
				TestCase.assertEquals(p + "incorrect escalation type - task "
						+ t + ", escalation " + e, ee.getEscalationType(), ae
						.getEscalationType());
			}
		}
	}

	/**
	 * Convenience method that loads the {@link WorkModel} by instantiating an
	 * instance of the {@link WorkModel} and deriving the {@link ClassLoader}
	 * from it.
	 * 
	 * @param workLoaderClass
	 *            Class of the {@link WorkLoader}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkLoader}.
	 * @return Loaded {@link WorkModel}.
	 * @throws Exception
	 *             If fails to load the {@link WorkModel}.
	 */
	public static WorkModel<?> loadWork(
			Class<? extends WorkLoader> workLoaderClass,
			String... propertyNameValues) throws Exception {

		// Create an instance of the work loader
		WorkLoader workLoader = workLoaderClass.newInstance();

		// Return the loaded work
		return loadWork(workLoader, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link WorkModel} by obtaining the
	 * {@link ClassLoader} from the {@link WorkLoader} class.
	 * 
	 * @param workLoader
	 *            {@link WorkLoader}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkLoader}.
	 * @return Loaded {@link WorkModel}.
	 * @throws Exception
	 *             If fails to load the {@link WorkModel}.
	 */
	public static WorkModel<?> loadWork(WorkLoader workLoader,
			String... propertyNameValues) throws Exception {

		// Obtain the class loader from the work loader
		ClassLoader classLoader = workLoader.getClass().getClassLoader();

		// Return the loaded work
		return loadWork(workLoader, classLoader, propertyNameValues);
	}

	/**
	 * Loads the {@link WorkModel} for the {@link WorkLoader} given the input
	 * details.
	 * 
	 * @param workLoader
	 *            {@link WorkLoader}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkLoader}.
	 * @return Loaded {@link WorkModel}.
	 * @throws Exception
	 *             If fails to load the {@link WorkModel}.
	 */
	public static WorkModel<?> loadWork(WorkLoader workLoader,
			ClassLoader classLoader, String... propertyNameValues)
			throws Exception {

		// Obtain the property name values
		if ((propertyNameValues.length % 2) != 0) {
			throw new IllegalArgumentException(
					"Must have even listing of property name values");
		}
		String[] propertyNames = new String[propertyNameValues.length / 2];
		Properties properties = new Properties();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String propertyName = propertyNameValues[i];
			String propertyValue = propertyNameValues[i + 1];
			propertyNames[i / 2] = propertyName;
			properties.setProperty(propertyName, propertyValue);
		}

		// Create the work loader context
		WorkLoaderContext context = new WorkLoaderContextImpl(propertyNames,
				properties, classLoader);

		// Load the work
		WorkModel<?> work = workLoader.loadWork(context);

		// Return the loaded work
		return work;
	}

	/**
	 * All access via static methods.
	 */
	private WorkLoaderUtil() {
	}
}
