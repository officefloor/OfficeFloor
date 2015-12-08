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
package net.officefloor.plugin.jndi.work;

import java.util.Date;

import javax.naming.Context;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlUnmarshaller;

/**
 * Tests the {@link JndiWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JndiWorkSourceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(JndiWorkSource.class,
				JndiWorkSource.PROPERTY_JNDI_NAME, "JNDI Name",
				JndiWorkSource.PROPERTY_WORK_TYPE, "Work Type");
	}

	/**
	 * Validates the type.
	 */
	public void testType() {

		// Create the work
		WorkTypeBuilder<JndiWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new JndiWorkFactory(null, null));

		// Create the complex task
		TaskTypeBuilder<Indexed, None> complexTask = work.addTaskType(
				"complexTask", new JndiObjectTaskFactory(null, false, null),
				Indexed.class, None.class);
		complexTask.addObject(Context.class).setLabel(Context.class.getName());
		complexTask.addObject(String.class).setLabel(String.class.getName());
		complexTask.addObject(XmlUnmarshaller.class).setLabel(
				XmlUnmarshaller.class.getName());
		complexTask.setReturnType(long.class);
		complexTask.addEscalation(XmlMarshallException.class);

		// Create the simple task
		work.addTaskType("simpleTask",
				new JndiObjectTaskFactory(null, false, null), Indexed.class,
				None.class).addObject(Context.class)
				.setLabel(Context.class.getName());

		// Validate the type
		WorkLoaderUtil.validateWorkType(work, JndiWorkSource.class,
				JndiWorkSource.PROPERTY_JNDI_NAME, "mock/JndiObject",
				JndiWorkSource.PROPERTY_WORK_TYPE,
				MockJndiObject.class.getName());
	}

	/**
	 * Validates the type with an adapter provided.
	 */
	public void testTypeWithAdapter() {

		// Create the work
		WorkTypeBuilder<JndiWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(new JndiWorkFactory(null, null));

		// Create the complex facade
		TaskTypeBuilder<Indexed, None> complexTask = work.addTaskType(
				"complexFacade", new JndiFacadeTaskFactory(null, false, null),
				Indexed.class, None.class);
		complexTask.addObject(Context.class).setLabel(Context.class.getName());
		complexTask.addObject(String.class).setLabel(String.class.getName());
		complexTask.addObject(Integer.class).setLabel(Integer.class.getName());
		complexTask.setReturnType(Date.class);
		complexTask.addEscalation(Exception.class);

		// Ensure override by name
		work.addTaskType("complexTask",
				new JndiFacadeTaskFactory(null, false, null), Indexed.class,
				None.class).addObject(Context.class)
				.setLabel(Context.class.getName());

		// Create the simple facade
		work.addTaskType("simpleFacade",
				new JndiFacadeTaskFactory(null, false, null), Indexed.class,
				None.class).addObject(Context.class)
				.setLabel(Context.class.getName());

		// Create the simple task
		work.addTaskType("simpleTask",
				new JndiObjectTaskFactory(null, false, null), Indexed.class,
				None.class).addObject(Context.class)
				.setLabel(Context.class.getName());

		// Validate the type
		WorkLoaderUtil.validateWorkType(work, JndiWorkSource.class,
				JndiWorkSource.PROPERTY_JNDI_NAME, "mock/JndiObject",
				JndiWorkSource.PROPERTY_WORK_TYPE,
				MockJndiObject.class.getName(),
				JndiWorkSource.PROPERTY_FACADE_CLASS,
				MockFacade.class.getName());
	}

	/**
	 * Ensure can execute the JNDI Object {@link Task}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testExecuteJndiObjectTask() throws Throwable {

		final String JNDI_NAME = "mock/JndiObject";
		final Context context = this.createMock(Context.class);
		final String XML = "<test/>";
		final XmlUnmarshaller unmarshaller = this
				.createMock(XmlUnmarshaller.class);
		final MockJndiObject jndiObject = this.createMock(MockJndiObject.class);
		final Long RETURN_VALUE = new Long(100);

		// Record looking up JNDI object and executing its method
		this.recordReturn(context, context.lookup(JNDI_NAME), jndiObject);
		this.recordReturn(jndiObject,
				jndiObject.complexTask(XML, unmarshaller), RETURN_VALUE);

		// Test
		this.replayMockObjects();

		// Load the work type
		WorkType<JndiWork> work = WorkLoaderUtil.loadWorkType(
				JndiWorkSource.class, JndiWorkSource.PROPERTY_JNDI_NAME,
				JNDI_NAME, JndiWorkSource.PROPERTY_WORK_TYPE,
				MockJndiObject.class.getName());

		// String Office
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the team
		this.constructTeam("TEAM", new PassiveTeam());

		// Construct the Context ManagedObject
		this.constructManagedObject(context, "CONTEXT_MOS", officeName);
		officeBuilder.addProcessManagedObject("CONTEXT_MO", "CONTEXT_MOS");

		// Construct the XML ManagedObject
		this.constructManagedObject(XML, "XML_MOS", officeName);
		officeBuilder.addProcessManagedObject("XML_MO", "XML_MOS");

		// Construct the Unmarshaller ManagedObject
		this.constructManagedObject(unmarshaller, "UNMARSHALLER_MOS",
				officeName);
		officeBuilder.addProcessManagedObject("UNMARSHALLER_MO",
				"UNMARSHALLER_MOS");

		// Register the work and task (complexTask)
		WorkBuilder<JndiWork> workBuilder = this.constructWork("WORK",
				work.getWorkFactory());
		workBuilder.setInitialTask("TASK");
		TaskFactory<JndiWork, ?, ?> taskFactory = work.getTaskTypes()[0]
				.getTaskFactory();
		TaskBuilder task = this.constructTask("TASK", taskFactory, "TEAM");
		task.linkManagedObject(0, "CONTEXT_MO", Context.class);
		task.linkManagedObject(1, "XML_MO", String.class);
		task.linkManagedObject(2, "UNMARSHALLER_MO", XmlUnmarshaller.class);

		// Invoke the Task
		this.invokeWork("WORK", null);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure can execute the facade {@link Task}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testExecuteJndiFacadeTask() throws Throwable {

		final String JNDI_NAME = "mock/JndiObject";
		final Context context = this.createMock(Context.class);
		final String XML = "<test/>";
		final Integer IDENTIFIER = Integer.valueOf(1);
		final XmlUnmarshaller unmarshaller = this
				.createMock(XmlUnmarshaller.class);
		final MockJndiObject jndiObject = this.createMock(MockJndiObject.class);
		final Long RETURN_VALUE = new Long(100);

		// Register the unmarshaller against identifier
		MockFacade.reset();
		MockFacade.registerXmlUnmarshaller(IDENTIFIER, unmarshaller);

		// Record looking up JNDI object and executing its method
		this.recordReturn(context, context.lookup(JNDI_NAME), jndiObject);
		this.recordReturn(jndiObject,
				jndiObject.complexTask(XML, unmarshaller), RETURN_VALUE);

		// Test
		this.replayMockObjects();

		// Load the work type (with facade)
		WorkType<JndiWork> work = WorkLoaderUtil.loadWorkType(
				JndiWorkSource.class, JndiWorkSource.PROPERTY_JNDI_NAME,
				JNDI_NAME, JndiWorkSource.PROPERTY_WORK_TYPE,
				MockJndiObject.class.getName(),
				JndiWorkSource.PROPERTY_FACADE_CLASS,
				MockFacade.class.getName());

		// String Office
		String officeName = this.getOfficeName();
		OfficeBuilder officeBuilder = this.getOfficeBuilder();

		// Construct the team
		this.constructTeam("TEAM", new PassiveTeam());

		// Construct the Context ManagedObject
		this.constructManagedObject(context, "CONTEXT_MOS", officeName);
		officeBuilder.addProcessManagedObject("CONTEXT_MO", "CONTEXT_MOS");

		// Construct the XML ManagedObject
		this.constructManagedObject(XML, "XML_MOS", officeName);
		officeBuilder.addProcessManagedObject("XML_MO", "XML_MOS");

		// Construct the Identifier ManagedObject
		this.constructManagedObject(IDENTIFIER, "IDENTIFIER_MOS", officeName);
		officeBuilder
				.addProcessManagedObject("IDENTIFIER_MO", "IDENTIFIER_MOS");

		// Register the work and task (complexTask)
		WorkBuilder<JndiWork> workBuilder = this.constructWork("WORK",
				work.getWorkFactory());
		workBuilder.setInitialTask("TASK");
		TaskFactory<JndiWork, ?, ?> taskFactory = work.getTaskTypes()[0]
				.getTaskFactory();
		TaskBuilder task = this.constructTask("TASK", taskFactory, "TEAM");
		task.linkManagedObject(0, "CONTEXT_MO", Context.class);
		task.linkManagedObject(1, "XML_MO", String.class);
		task.linkManagedObject(2, "IDENTIFIER_MO", Integer.class);

		// Invoke the Task
		this.invokeWork("WORK", null);

		// Verify functionality
		this.verifyMockObjects();
	}

}