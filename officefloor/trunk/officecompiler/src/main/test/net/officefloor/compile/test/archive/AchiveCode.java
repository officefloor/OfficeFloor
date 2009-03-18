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
package net.officefloor.compile.test.archive;

import junit.framework.TestCase;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.frame.api.build.WorkFactory;

/**
 * Archived code.
 * 
 * @author Daniel
 */
// Remove all this code once compiler finished
@Deprecated
public class AchiveCode {

	/**
	 * Asserts the {@link WorkType} instances match.
	 * 
	 * @param expected
	 *            Expected {@link WorkType}.
	 * @param actual
	 *            Actual {@link WorkType}.
	 * @see #assertWorkModelMatch(String, WorkType, WorkType)
	 */
	public static void assertWorkModelMatch(WorkType<?> expected,
			WorkType<?> actual) {
		assertWorkModelMatch("", expected, actual);
	}

	/**
	 * <p>
	 * Asserts the {@link WorkType} instances match.
	 * <p>
	 * Note that this is not a complete equals as items such as
	 * {@link WorkFactory} and {@link TaskFactoryManufacturer} are only checked
	 * to be of same types.
	 * 
	 * @param messagePrefix
	 *            Prefix given to assertion messages.
	 * @param expected
	 *            Expected {@link WorkType}.
	 * @param actual
	 *            Actual {@link WorkType}.
	 */
	public static void assertWorkModelMatch(String messagePrefix,
			WorkType<?> expected, WorkType<?> actual) {

		// Provide smaller names to make code easier to read
		final String p = messagePrefix;
		final WorkType<?> ew = expected;
		final WorkType<?> aw = actual;

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

}