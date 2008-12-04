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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.model.work.TaskEscalationModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.AbstractWorkLoader;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkLoaderContext;
import net.officefloor.work.http.HttpException;

/**
 * {@link WorkLoader} for the {@link HttpHtmlFormToBeanTask}.
 * 
 * @author Daniel
 */
public class HttpHtmlFormToBeanWorkLoader extends AbstractWorkLoader {

	/**
	 * Property specifying the class of the bean.
	 */
	public static final String BEAN_CLASS_PROPERTY = "bean.class";

	/**
	 * Property prefix for an alias.
	 */
	public static final String ALIAS_PROPERTY_PREFIX = "alias.";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.work.AbstractWorkLoader#loadSpecification(net.officefloor
	 * .work.AbstractWorkLoader.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(BEAN_CLASS_PROPERTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.work.WorkLoader#loadWork(net.officefloor.work.
	 * WorkLoaderContext)
	 */
	@Override
	public WorkModel<?> loadWork(WorkLoaderContext context) throws Exception {

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

		// Create the work factory
		HttpHtmlFormToBeanTask workFactory = new HttpHtmlFormToBeanTask(
				beanClass, aliases);

		// Create the work model
		WorkModel<HttpHtmlFormToBeanTask> work = new WorkModel<HttpHtmlFormToBeanTask>();
		work.setTypeOfWork(HttpHtmlFormToBeanTask.class);
		work.setWorkFactory(workFactory);

		// Create the task model
		TaskModel<Indexed, None> task = new TaskModel<Indexed, None>();
		task.setTaskName("MapFormToBean");
		task.setTaskFactoryManufacturer(workFactory);
		work.addTask(task);

		// Link to HTTP connection for request
		TaskObjectModel<Indexed> object = new TaskObjectModel<Indexed>();
		object.setObjectType(ServerHttpConnection.class.getName());
		task.addObject(object);

		// Add the escalations
		task.addEscalation(new TaskEscalationModel(HttpException.class
				.getName()));
		task.addEscalation(new TaskEscalationModel(BeanMapException.class
				.getName()));

		// Return the work
		return work;
	}

}
