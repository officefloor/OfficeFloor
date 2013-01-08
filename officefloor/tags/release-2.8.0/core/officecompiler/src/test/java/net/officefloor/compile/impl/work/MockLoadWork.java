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
package net.officefloor.compile.impl.work;

import java.io.IOException;

import junit.framework.TestCase;

import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Class for {@link ClassWorkSource} that enables validating loading a
 * {@link WorkType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadWork {

	/**
	 * Asserts the loaded {@link WorkType} is correct.
	 * 
	 * @param workType
	 *            {@link WorkType} to validate.
	 */
	public static void assertWorkType(WorkType<?> workType) {

		// Ensure correct number of tasks
		TestCase.assertEquals("Incorrect number of tasks", 2, workType
				.getTaskTypes().length);

		// Ensure correct first task
		TaskType<?, ?, ?> taskOne = workType.getTaskTypes()[0];
		TestCase.assertEquals("Incorrect first task", "assertWorkType", taskOne
				.getTaskName());
		TestCase.assertEquals("Incorrect number of flows", 0, taskOne
				.getFlowTypes().length);
		TestCase.assertEquals("Incorrect number of objects", 1, taskOne
				.getObjectTypes().length);
		TestCase.assertEquals("Incorrect object type", WorkType.class, taskOne
				.getObjectTypes()[0].getObjectType());
		TestCase.assertEquals("Incorrect number of escalations", 0, taskOne
				.getEscalationTypes().length);

		// Ensure correct second task
		TaskType<?, ?, ?> taskTwo = workType.getTaskTypes()[1];
		TestCase.assertEquals("Incorrect second task", "doTask", taskTwo
				.getTaskName());
		TestCase.assertEquals("Incorrect number of flows", 0, taskTwo
				.getFlowTypes().length);
		TestCase.assertEquals("Incorrect number of objects", 1, taskTwo
				.getObjectTypes().length);
		TestCase.assertEquals("Incorrect object type", Integer.class, taskTwo
				.getObjectTypes()[0].getObjectType());
		TestCase.assertEquals("Incorrect number of escalations", 1, taskTwo
				.getEscalationTypes().length);
		TestCase.assertEquals("Incorrect escalation type", IOException.class,
				taskTwo.getEscalationTypes()[0].getEscalationType());
		TestCase.assertEquals("Incorrect return type", String.class, taskTwo
				.getReturnType());
	}

	/**
	 * Mock task method.
	 * 
	 * @param object
	 *            Object.
	 * @return Value for parameter.
	 * @throws IOException
	 *             Escalation.
	 */
	public String doTask(Integer object) throws IOException {
		return "test";
	}

}