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
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.jdbc.ConnectionValidator;

/**
 * {@link ManagedFunction} providing testing of a {@link Connection} from a
 * {@link DataSourceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataSourceManagedFunction extends StaticManagedFunction<Indexed, None> {

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
	public DataSourceManagedFunction(ConnectionValidator validator) {
		this.validator = validator;
	}

	/**
	 * Constructs this {@link ManagedFunction}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder} to construct against.
	 * @param namePrefix
	 *            Prefix for {@link ManagedFunction} name.
	 * @param dataSourceMoName
	 *            Name of the {@link DataSourceManagedObjectSource}.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return Name of {@link ManagedFunction}.
	 */
	public String construct(OfficeBuilder officeBuilder, String namePrefix, String dataSourceMoName, String teamName)
			throws Exception {

		// Ensure have a name prefix
		namePrefix = (namePrefix == null ? "" : namePrefix);

		// Create the function name
		String functionName = namePrefix + "Function";

		// Configure the function
		ManagedFunctionBuilder<Indexed, None> functionBuilder = officeBuilder.addManagedFunction(functionName, this);
		functionBuilder.linkManagedObject(0, "mo", DataSource.class);
		functionBuilder.setResponsibleTeam(teamName);

		// Return the work name
		return functionName;
	}

	/*
	 * =================== ManagedFunction =========================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

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