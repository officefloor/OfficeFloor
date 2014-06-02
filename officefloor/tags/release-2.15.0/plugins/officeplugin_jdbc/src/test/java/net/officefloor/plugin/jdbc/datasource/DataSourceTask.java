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
package net.officefloor.plugin.jdbc.datasource;

import java.sql.Connection;

import javax.sql.DataSource;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.jdbc.ConnectionValidator;

/**
 * {@link Task} providing testing of a {@link Connection} from a
 * {@link DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceTask extends AbstractSingleTask<Work, Indexed, None> {

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
	public DataSourceTask(ConnectionValidator validator) {
		this.validator = validator;
	}

	/**
	 * Constructs this {@link Task}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder} to construct against.
	 * @param namePrefix
	 *            Prefix for {@link Work} and {@link Task} names.
	 * @param dataSourceMoName
	 *            Name of the {@link DataSourceManagedObjectSource}.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return Name of {@link Work} it is registered under so may be invoked.
	 */
	public String construct(OfficeBuilder officeBuilder, String namePrefix,
			String dataSourceMoName, String teamName) throws Exception {

		// Ensure have a name prefix
		namePrefix = (namePrefix == null ? "" : namePrefix);

		// Create the work name
		String workName = namePrefix + "Work";

		// Configure the Work
		WorkBuilder<Work> workBuilder = this.registerWork(workName,
				officeBuilder);
		workBuilder.addWorkManagedObject("mo", dataSourceMoName);

		// Configure the Task
		TaskBuilder<Work, Indexed, None> taskBuilder = this.registerTask(
				"Task", teamName, workBuilder);
		taskBuilder.linkManagedObject(0, "mo", DataSource.class);

		// Return the work name
		return workName;
	}

	/*
	 * =================== AbstractSingleTask =========================
	 */

	@Override
	public Object doTask(TaskContext<Work, Indexed, None> context)
			throws Throwable {

		// Obtain the DataSource
		DataSource dataSource = (DataSource) context.getObject(0);
		TestCase.assertNotNull("Must have DataSource", dataSource);

		// Obtain the connection
		Connection connection = dataSource.getConnection();

		// Validate the connection
		this.validator.validateConnection(connection);

		// Close the connection
		connection.close();

		// Nothing to return
		return null;
	}

}