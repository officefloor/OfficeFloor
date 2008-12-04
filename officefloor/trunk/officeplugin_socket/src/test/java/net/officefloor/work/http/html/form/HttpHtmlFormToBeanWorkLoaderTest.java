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
package net.officefloor.work.http.html.form;

import java.lang.reflect.Method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.WorkLoaderUtil;
import net.officefloor.work.http.HttpException;

/**
 * Tests the {@link HttpHtmlFormToBeanWorkLoader}.
 * 
 * @author Daniel
 */
public class HttpHtmlFormToBeanWorkLoaderTest extends OfficeFrameTestCase {

	/**
	 * Validates correctly loaded {@link WorkModel}.
	 */
	public void testLoadWork() throws Exception {

		// Load the work
		WorkModel<?> actualWork = WorkLoaderUtil.loadWork(
				HttpHtmlFormToBeanWorkLoader.class,
				HttpHtmlFormToBeanWorkLoader.BEAN_CLASS_PROPERTY,
				HtmlFormBean.class.getName());

		// Create the expected work
		HttpHtmlFormToBeanTask formToBean = new HttpHtmlFormToBeanTask(
				HtmlFormBean.class, null);
		WorkModel<HttpHtmlFormToBeanTask> work = new WorkModel<HttpHtmlFormToBeanTask>();
		work.setTypeOfWork(HttpHtmlFormToBeanTask.class);
		work.setWorkFactory(formToBean);

		// Create the expected task
		TaskModel<Indexed, None> task = new TaskModel<Indexed, None>();
		work.addTask(task);
		task.setTaskName("MapFormToBean");
		task.setTaskFactoryManufacturer(formToBean);

		// Add the HTTP managed object
		TaskObjectModel<Indexed> object = new TaskObjectModel<Indexed>();
		task.addObject(object);
		object.setObjectType(ServerHttpConnection.class.getName());

		// Add the escalations
		task.addEscalation(new TaskEscalationModel(HttpException.class
				.getName()));
		task.addEscalation(new TaskEscalationModel(BeanMapException.class
				.getName()));

		// Verify work
		WorkLoaderUtil.assertWorkModelMatch(work, actualWork);
	}

	/**
	 * Ensures that alias mappings loaded.
	 */
	public void testAliasLoaded() throws Throwable {

		// Load the work
		WorkModel<?> work = WorkLoaderUtil.loadWork(
				HttpHtmlFormToBeanWorkLoader.class,
				HttpHtmlFormToBeanWorkLoader.BEAN_CLASS_PROPERTY,
				HtmlFormBean.class.getName(),
				HttpHtmlFormToBeanWorkLoader.ALIAS_PROPERTY_PREFIX + "another",
				"name");

		// Obtain the task
		HttpHtmlFormToBeanTask task = (HttpHtmlFormToBeanTask) work
				.getWorkFactory();

		// Ensure have alias mapping
		Method method = task.beanProperties.get("another");
		assertEquals("Expect to have alias mapping", "setName", method
				.getName());
	}
}
