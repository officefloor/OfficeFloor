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
package net.officefloor.plugin.jndi.function;

import java.util.Date;

import javax.naming.Context;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlUnmarshaller;

/**
 * Tests the {@link JndiManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiManagedFunctionSourceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(JndiManagedFunctionSource.class,
				JndiManagedFunctionSource.PROPERTY_JNDI_NAME, "JNDI Name",
				JndiManagedFunctionSource.PROPERTY_OBJECT_TYPE, "Object Type");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create the work
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Create the complex task
		ManagedFunctionTypeBuilder<Indexed, None> complexFunction = namespace.addManagedFunctionType("complexFunction",
				new JndiObjectManagedFunctionFactory(null, null, null), Indexed.class, None.class);
		complexFunction.addObject(Context.class).setLabel(Context.class.getName());
		complexFunction.addObject(String.class).setLabel(String.class.getName());
		complexFunction.addObject(XmlUnmarshaller.class).setLabel(XmlUnmarshaller.class.getName());
		complexFunction.setReturnType(long.class);
		complexFunction.addEscalation(XmlMarshallException.class);

		// Create the simple task
		namespace.addManagedFunctionType("simpleFunction", new JndiObjectManagedFunctionFactory(null, null, null),
				Indexed.class, None.class).addObject(Context.class).setLabel(Context.class.getName());

		// Validate the type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace, JndiManagedFunctionSource.class,
				JndiManagedFunctionSource.PROPERTY_JNDI_NAME, "mock/JndiObject",
				JndiManagedFunctionSource.PROPERTY_OBJECT_TYPE, MockJndiObject.class.getName());
	}

	/**
	 * Validates the type with a facade provided.
	 */
	public void testTypeWithFacade() {

		// Create the work
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Create the complex facade
		ManagedFunctionTypeBuilder<Indexed, None> complexTask = namespace.addManagedFunctionType("complexFacade",
				new JndiFacadeManagedFunctionFactory(null, null, false, null), Indexed.class, None.class);
		complexTask.addObject(Context.class).setLabel(Context.class.getName());
		complexTask.addObject(String.class).setLabel(String.class.getName());
		complexTask.addObject(Integer.class).setLabel(Integer.class.getName());
		complexTask.setReturnType(Date.class);
		complexTask.addEscalation(Exception.class);

		// Ensure override by name
		namespace
				.addManagedFunctionType("complexFunction",
						new JndiFacadeManagedFunctionFactory(null, null, false, null), Indexed.class, None.class)
				.addObject(Context.class).setLabel(Context.class.getName());

		// Create the simple facade
		namespace.addManagedFunctionType("simpleFacade", new JndiFacadeManagedFunctionFactory(null, null, false, null),
				Indexed.class, None.class).addObject(Context.class).setLabel(Context.class.getName());

		// Create the simple function
		namespace.addManagedFunctionType("simpleFunction", new JndiObjectManagedFunctionFactory(null, null, null),
				Indexed.class, None.class).addObject(Context.class).setLabel(Context.class.getName());

		// Validate the type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace, JndiManagedFunctionSource.class,
				JndiManagedFunctionSource.PROPERTY_JNDI_NAME, "mock/JndiObject",
				JndiManagedFunctionSource.PROPERTY_OBJECT_TYPE, MockJndiObject.class.getName(),
				JndiManagedFunctionSource.PROPERTY_FACADE_CLASS, MockFacade.class.getName());
	}

	/**
	 * Ensure can execute the JNDI Object {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testExecuteJndiObjectFunction() throws Throwable {

		final String JNDI_NAME = "mock/JndiObject";
		final Context context = this.createMock(Context.class);
		final String XML = "<test/>";
		final XmlUnmarshaller unmarshaller = this.createMock(XmlUnmarshaller.class);
		final MockJndiObject jndiObject = this.createMock(MockJndiObject.class);
		final Long RETURN_VALUE = new Long(100);

		// Record looking up JNDI object and executing its method
		this.recordReturn(context, context.lookup(JNDI_NAME), jndiObject);
		this.recordReturn(jndiObject, jndiObject.complexFunction(XML, unmarshaller), RETURN_VALUE);

		// Test
		this.replayMockObjects();

		// Load the namespace type
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				JndiManagedFunctionSource.class, JndiManagedFunctionSource.PROPERTY_JNDI_NAME, JNDI_NAME,
				JndiManagedFunctionSource.PROPERTY_OBJECT_TYPE, MockJndiObject.class.getName());

		// String Office
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the Context ManagedObject
		this.constructManagedObject(context, "CONTEXT_MOS", officeName);
		officeBuilder.addProcessManagedObject("CONTEXT_MO", "CONTEXT_MOS");

		// Construct the XML ManagedObject
		this.constructManagedObject(XML, "XML_MOS", officeName);
		officeBuilder.addProcessManagedObject("XML_MO", "XML_MOS");

		// Construct the Unmarshaller ManagedObject
		this.constructManagedObject(unmarshaller, "UNMARSHALLER_MOS", officeName);
		officeBuilder.addProcessManagedObject("UNMARSHALLER_MO", "UNMARSHALLER_MOS");

		// Register the function (complexFunction)
		ManagedFunctionFactory<?, ?> functionFactory = namespace.getManagedFunctionTypes()[0]
				.getManagedFunctionFactory();
		ManagedFunctionBuilder function = this.constructFunction("FUNCTION", functionFactory);
		function.linkManagedObject(0, "CONTEXT_MO", Context.class);
		function.linkManagedObject(1, "XML_MO", String.class);
		function.linkManagedObject(2, "UNMARSHALLER_MO", XmlUnmarshaller.class);

		// Invoke the function
		this.invokeFunction("FUNCTION", null);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure can execute the facade {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testExecuteJndiFacadeFunction() throws Throwable {

		final String JNDI_NAME = "mock/JndiObject";
		final Context context = this.createMock(Context.class);
		final String XML = "<test/>";
		final Integer IDENTIFIER = Integer.valueOf(1);
		final XmlUnmarshaller unmarshaller = this.createMock(XmlUnmarshaller.class);
		final MockJndiObject jndiObject = this.createMock(MockJndiObject.class);
		final Long RETURN_VALUE = new Long(100);

		// Register the unmarshaller against identifier
		MockFacade.reset();
		MockFacade.registerXmlUnmarshaller(IDENTIFIER, unmarshaller);

		// Record looking up JNDI object and executing its method
		this.recordReturn(context, context.lookup(JNDI_NAME), jndiObject);
		this.recordReturn(jndiObject, jndiObject.complexFunction(XML, unmarshaller), RETURN_VALUE);

		// Test
		this.replayMockObjects();

		// Load the namespace type (with facade)
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				JndiManagedFunctionSource.class, JndiManagedFunctionSource.PROPERTY_JNDI_NAME, JNDI_NAME,
				JndiManagedFunctionSource.PROPERTY_OBJECT_TYPE, MockJndiObject.class.getName(),
				JndiManagedFunctionSource.PROPERTY_FACADE_CLASS, MockFacade.class.getName());

		// String Office
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the Context ManagedObject
		this.constructManagedObject(context, "CONTEXT_MOS", officeName);
		officeBuilder.addProcessManagedObject("CONTEXT_MO", "CONTEXT_MOS");

		// Construct the XML ManagedObject
		this.constructManagedObject(XML, "XML_MOS", officeName);
		officeBuilder.addProcessManagedObject("XML_MO", "XML_MOS");

		// Construct the Identifier ManagedObject
		this.constructManagedObject(IDENTIFIER, "IDENTIFIER_MOS", officeName);
		officeBuilder.addProcessManagedObject("IDENTIFIER_MO", "IDENTIFIER_MOS");

		// Register the function (complexFunction)
		ManagedFunctionFactory<?, ?> functionFactory = namespace.getManagedFunctionTypes()[0]
				.getManagedFunctionFactory();
		ManagedFunctionBuilder function = this.constructFunction("FUNCTION", functionFactory);
		function.linkManagedObject(0, "CONTEXT_MO", Context.class);
		function.linkManagedObject(1, "XML_MO", String.class);
		function.linkManagedObject(2, "IDENTIFIER_MO", Integer.class);

		// Invoke the function
		this.invokeFunction("FUNCTION", null);

		// Verify functionality
		this.verifyMockObjects();
	}

}