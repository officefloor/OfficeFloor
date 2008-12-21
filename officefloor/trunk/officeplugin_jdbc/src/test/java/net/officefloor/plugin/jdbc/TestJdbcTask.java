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
package net.officefloor.plugin.jdbc;

import java.sql.Connection;

import junit.framework.TestCase;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;

/**
 * {@link Task} providing testing of a {@link Connection} from a
 * {@link JdbcManagedObjectSource}.
 * 
 * @author Daniel
 */
public class TestJdbcTask extends AbstractSingleTask<Object, Work, None, None> {

	/**
	 * {@link ConnectionValidator}.
	 */
	private final ConnectionValidator validator;

	/**
	 * Initiate.
	 * 
	 * @param validator
	 *            {@link ConnectionValidator}.
	 */
	public TestJdbcTask(ConnectionValidator validator) {
		this.validator = validator;
	}

	/**
	 * Constructs the {@link TestTask}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder} to construct against.
	 * @param namePrefix
	 *            Prefix for {@link Work} and {@link Task} names.
	 * @param jdbcMoName
	 *            Name of the {@link JdbcManagedObjectSource}.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return Name of {@link Work} it is registered under so may be invoked.
	 */
	public String construct(OfficeBuilder officeBuilder, String namePrefix,
			String jdbcMoName, String teamName) throws Exception {

		// Ensure have a name prefix
		namePrefix = (namePrefix == null ? "" : namePrefix);

		// Create the work name
		String workName = namePrefix + "Work";

		// Configure the Work
		WorkBuilder<?> workBuilder = this.registerWork(workName, officeBuilder);
		workBuilder.addWorkManagedObject("mo", jdbcMoName);

		// Configure the Task
		TaskBuilder<?, ?, ?, ?> taskBuilder = this.registerTask("Task",
				teamName, workBuilder);
		taskBuilder.linkManagedObject(0, "mo");

		// Return the work name
		return workName;
	}

	/*
	 * =================== AbstractSingleTask =========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame
	 * .api.execute.TaskContext)
	 */
	@Override
	public Object doTask(TaskContext<Object, Work, None, None> context)
			throws Throwable {

		// Obtain the Connection
		Connection connection = (Connection) context.getObject(0);
		TestCase.assertNotNull("Must have connection", connection);

		// Validate the connection
		this.validator.validateConnection(connection);

		// Nothing to return
		return null;
	}

}
