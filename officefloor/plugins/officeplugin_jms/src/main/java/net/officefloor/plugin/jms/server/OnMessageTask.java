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
package net.officefloor.plugin.jms.server;

import javax.jms.Message;

import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Handles obtaining the {@link Message}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnMessageTask
		extends
		AbstractSingleTask<Work, OnMessageTask.OnMessageDependencies, OnMessageTask.OnMessageFlows> {

	/**
	 * Keys for the {@link OnMessageTask} dependencies.
	 */
	public enum OnMessageDependencies {
		JMS_SERVER_MANAGED_OBJECT
	}

	/**
	 * Keys for flow instigated by the {@link OnMessageTask} with the
	 * {@link Message} as argument.
	 */
	public enum OnMessageFlows {
		ON_MESSAGE
	}

	/*
	 * =========================== Task ================================
	 */

	@Override
	public Object execute(
			ManagedFunctionContext<Work, OnMessageDependencies, OnMessageFlows> context)
			throws Exception {

		// Obtain the JMS Server Managed Object
		JmsServerManagedObject mo = (JmsServerManagedObject) context
				.getObject(OnMessageDependencies.JMS_SERVER_MANAGED_OBJECT);

		// Run the session to source the message
		mo.getSession().run();

		// Obtain the Message
		Message message = mo.getMessage();

		// Process the message
		context.doFlow(OnMessageFlows.ON_MESSAGE, message);

		// Not expected to have next task
		return null;
	}

}