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
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Class for {@link ClassWorkSource} that enables validating loading a
 * {@link FunctionNamespaceType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadWork {

	/**
	 * Asserts the loaded {@link FunctionNamespaceType} is correct.
	 * 
	 * @param workType
	 *            {@link FunctionNamespaceType} to validate.
	 */
	public static void assertWorkType(FunctionNamespaceType<?> workType) {

		// Ensure correct number of tasks
		TestCase.assertEquals("Incorrect number of tasks", 2, workType
				.getManagedFunctionTypes().length);

		// Ensure correct first task
		ManagedFunctionType<?, ?, ?> taskOne = workType.getManagedFunctionTypes()[0];
		TestCase.assertEquals("Incorrect first task", "assertWorkType", taskOne
				.getFunctionName());
		TestCase.assertEquals("Incorrect number of flows", 0, taskOne
				.getFlowTypes().length);
		TestCase.assertEquals("Incorrect number of objects", 1, taskOne
				.getObjectTypes().length);
		TestCase.assertEquals("Incorrect object type", FunctionNamespaceType.class, taskOne
				.getObjectTypes()[0].getObjectType());
		TestCase.assertEquals("Incorrect number of escalations", 0, taskOne
				.getEscalationTypes().length);

		// Ensure correct second task
		ManagedFunctionType<?, ?, ?> taskTwo = workType.getManagedFunctionTypes()[1];
		TestCase.assertEquals("Incorrect second task", "doTask", taskTwo
				.getFunctionName());
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