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

package net.officefloor.compile.impl.managedfunction;

import java.io.IOException;

import org.junit.Assert;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;

/**
 * Class for {@link ClassManagedFunctionSource} that enables validating loading
 * a {@link FunctionNamespaceType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadFunctionNamespace {

	/**
	 * Asserts the loaded {@link FunctionNamespaceType} is correct.
	 * 
	 * @param namespaceType
	 *            {@link FunctionNamespaceType} to validate.
	 */
	public static void assertFunctionNamespaceType(FunctionNamespaceType namespaceType) {

		// Ensure correct number of functions
		Assert.assertEquals("Incorrect number of functions", 2, namespaceType.getManagedFunctionTypes().length);

		// Ensure correct first function
		ManagedFunctionType<?, ?> functionOne = namespaceType.getManagedFunctionTypes()[0];
		Assert.assertEquals("Incorrect first function", "assertFunctionNamespaceType", functionOne.getFunctionName());
		Assert.assertEquals("Incorrect number of flows", 0, functionOne.getFlowTypes().length);
		Assert.assertEquals("Incorrect number of objects", 1, functionOne.getObjectTypes().length);
		Assert.assertEquals("Incorrect object type", FunctionNamespaceType.class,
				functionOne.getObjectTypes()[0].getObjectType());
		Assert.assertEquals("Incorrect number of escalations", 0, functionOne.getEscalationTypes().length);

		// Ensure correct second function
		ManagedFunctionType<?, ?> functionTwo = namespaceType.getManagedFunctionTypes()[1];
		Assert.assertEquals("Incorrect second function", "doFunction", functionTwo.getFunctionName());
		Assert.assertEquals("Incorrect number of flows", 0, functionTwo.getFlowTypes().length);
		Assert.assertEquals("Incorrect number of objects", 1, functionTwo.getObjectTypes().length);
		Assert.assertEquals("Incorrect object type", Integer.class, functionTwo.getObjectTypes()[0].getObjectType());
		Assert.assertEquals("Incorrect number of escalations", 1, functionTwo.getEscalationTypes().length);
		Assert.assertEquals("Incorrect escalation type", IOException.class,
				functionTwo.getEscalationTypes()[0].getEscalationType());
		Assert.assertEquals("Incorrect return type", String.class, functionTwo.getReturnType());
	}

	/**
	 * Mock function method.
	 * 
	 * @param object
	 *            Object.
	 * @return Value for parameter.
	 * @throws IOException
	 *             Escalation.
	 */
	public String doFunction(Integer object) throws IOException {
		return "test";
	}

}
