/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.form;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.HttpException;
import net.officefloor.plugin.work.http.html.form.HttpHtmlFormToBeanTask.HttpHtmlFormToBeanTaskDependencies;

/**
 * {@link WorkSource} for the {@link HttpHtmlFormToBeanTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHtmlFormToBeanWorkSource extends
		AbstractWorkSource<HttpHtmlFormToBeanTask> {

	/**
	 * Property specifying the class of the bean.
	 */
	public static final String BEAN_CLASS_PROPERTY = "bean.class";

	/**
	 * Property prefix for an alias.
	 */
	public static final String ALIAS_PROPERTY_PREFIX = "alias.";

	/*
	 * ================== AbstractWorkSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(BEAN_CLASS_PROPERTY, "Bean Class");
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpHtmlFormToBeanTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the bean class
		String beanClassName = context.getProperty(BEAN_CLASS_PROPERTY);
		Class<?> beanClass = context.getClassLoader().loadClass(beanClassName);

		// Create the listing of aliases
		Map<String, String> aliases = new HashMap<String, String>();
		for (String name : context.getPropertyNames()) {

			// Determine if alias property
			if (!name.startsWith(ALIAS_PROPERTY_PREFIX)) {
				continue;
			}

			// Obtain the alias and corresponding property name
			String alias = name.substring(ALIAS_PROPERTY_PREFIX.length());
			String propertyName = context.getProperty(name);

			// Add the alias mapping
			aliases.put(alias, propertyName);
		}

		// Create the task
		HttpHtmlFormToBeanTask formToBeanTask = new HttpHtmlFormToBeanTask(
				beanClass, aliases);

		// Define the work
		workTypeBuilder.setWorkFactory(formToBeanTask);
		TaskTypeBuilder<HttpHtmlFormToBeanTaskDependencies, None> taskBuilder = workTypeBuilder
				.addTaskType("MapFormToBean", formToBeanTask,
						HttpHtmlFormToBeanTaskDependencies.class, None.class);
		taskBuilder.addObject(ServerHttpConnection.class).setKey(
				HttpHtmlFormToBeanTaskDependencies.SERVER_HTTP_CONNECTION);
		taskBuilder.addEscalation(HttpException.class);
		taskBuilder.addEscalation(BeanMapException.class);
	}

}