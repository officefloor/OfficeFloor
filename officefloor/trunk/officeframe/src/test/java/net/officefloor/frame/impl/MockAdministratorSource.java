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
package net.officefloor.frame.impl;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSourceSpecification;

/**
 * Mock implementation of the
 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}
 * for testing.
 * 
 * @author Daniel
 */
public class MockAdministratorSource implements AdministratorSource {

	/**
	 * Property name to source the {@link Administrator}.
	 */
	private static final String TASK_ADMINISTRATOR_PROPERTY = "net.officefloor.frame.construct.taskadministrator";

	/**
	 * Registry of the {@link Administrator} instances.
	 */
	private static final Map<String, TaskAdministratorSourceState> REGISTRY = new HashMap<String, TaskAdministratorSourceState>();

	/**
	 * Convenience method to bind the {@link Administrator} instance to the
	 * {@link AdministratorBuilder}.
	 * 
	 * @param builder
	 *            {@link AdministratorBuilder} to bind the {@link Administrator}.
	 * @param name
	 *            Name to bind under.
	 * @param administrator
	 *            {@link Administrator} to bind.
	 * @param sourceMetaData
	 *            {@link AdministratorSourceMetaData} to bind.
	 * @throws BuildException
	 *             If bind fails.
	 */
	public static void bindAdministrator(AdministratorBuilder<?> builder,
			String name, Administrator administrator,
			AdministratorSourceMetaData sourceMetaData) throws BuildException {

		// Specify the task administrator source class
		builder.setAdministratorSourceClass(MockAdministratorSource.class);

		// Provide task administrator link to meta-data
		builder.addProperty(TASK_ADMINISTRATOR_PROPERTY, name);

		// Create the state
		TaskAdministratorSourceState state = new TaskAdministratorSourceState();
		state.taskAdministrator = administrator;
		state.taskAdministratorSourceMetaData = sourceMetaData;

		// Bind the task administrator in registry
		REGISTRY.put(name, state);
	}

	/**
	 * {@link TaskAdministratorSourceState}.
	 */
	protected TaskAdministratorSourceState taskAdministratorSourceState;

	/**
	 * Default constructor.
	 */
	public MockAdministratorSource() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.taskadministration.source.TaskAdministratorSource#getSpecification()
	 */
	public AdministratorSourceSpecification getSpecification() {
		throw new UnsupportedOperationException(
				"Not supported by mock implementation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#init(net.officefloor.frame.spi.administration.source.AdministratorSourceContext)
	 */
	public void init(AdministratorSourceContext context) throws Exception {
		// Obtain the name of the Task Administrator
		String name = context.getProperties().getProperty(
				TASK_ADMINISTRATOR_PROPERTY);

		// Ensure have Task Administrator
		if (name == null) {
			throw new Exception("Property '" + TASK_ADMINISTRATOR_PROPERTY
					+ "' must be specified - likely that not bound.");
		}

		// Obtain the Task Administrator Source State
		this.taskAdministratorSourceState = REGISTRY.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.taskadministration.source.TaskAdministratorSource#getMetaData()
	 */
	public AdministratorSourceMetaData getMetaData() {
		return this.taskAdministratorSourceState.taskAdministratorSourceMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.administration.source.AdministratorSource#createAdministrator()
	 */
	public Administrator createAdministrator() {
		return this.taskAdministratorSourceState.taskAdministrator;
	}

}

/**
 * State of the
 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
 */
class TaskAdministratorSourceState {

	/**
	 * {@link Administrator}.
	 */
	Administrator taskAdministrator;

	/**
	 * {@link AdministratorSourceMetaData}.
	 */
	AdministratorSourceMetaData taskAdministratorSourceMetaData;

}