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
package net.officefloor.plugin.jms.server;

import javax.jms.Message;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * Handles obtaining the {@link javax.jms.Message}.
 * 
 * @author Daniel
 */
class OnMessageTask extends
		AbstractSingleTask<JmsServerManagedObject, Work, None, Indexed> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	public Object doTask(
			TaskContext<JmsServerManagedObject, Work, None, Indexed> context)
			throws Exception {

		// Obtain the JMS Server Managed Object
		JmsServerManagedObject mo = context.getParameter();

		// Run the session to source the managed object
		mo.getSession().run();

		// Obtain the Message
		Message message = mo.getMessage();

		// Return Message for next task to process
		return message;
	}

}
