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
package net.officefloor.frame.test;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.administration.source.AdministratorSource;
import net.officefloor.compile.spi.administration.source.AdministratorSourceContext;
import net.officefloor.compile.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.compile.spi.administration.source.AdministratorSourceSpecification;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.source.TestSource;

/**
 * Mock implementation of the {@link AdministratorSource} for testing.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockAdministratorSource implements AdministratorSource<Object, Indexed> {

	/**
	 * Property name to source the {@link Administration}.
	 */
	private static final String TASK_ADMINISTRATOR_PROPERTY = "net.officefloor.frame.construct.taskadministrator";

	/**
	 * Registry of the {@link Administration} instances.
	 */
	private static final Map<String, TaskAdministratorSourceState> REGISTRY = new HashMap<String, TaskAdministratorSourceState>();

	/**
	 * Convenience method to bind the {@link AdministrationDuty} instance to the
	 * {@link AdministrationBuilder}.
	 * 
	 * @param <A>
	 *            Administration key.
	 * @param name
	 *            Name to bind under.
	 * @param administrator
	 *            {@link AdministrationDuty} to bind.
	 * @param sourceMetaData
	 *            {@link AdministratorSourceMetaData} to bind.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @return {@link AdministrationBuilder} for additional configuration.
	 */
	@SuppressWarnings("unchecked")
	public static <A extends Enum<A>> AdministrationBuilder<A> bindAdministrator(String name,
			Administration<?, A> administrator, AdministratorSourceMetaData<?, A> sourceMetaData,
			OfficeBuilder officeBuilder) {

		// Create the administrator builder
		AdministrationBuilder<Indexed> builder = officeBuilder.addAdministrator(name, MockAdministratorSource.class);

		// Provide task administrator link to meta-data
		builder.addProperty(TASK_ADMINISTRATOR_PROPERTY, name);

		// Create the state
		TaskAdministratorSourceState state = new TaskAdministratorSourceState();
		state.taskAdministrator = administrator;
		state.taskAdministratorSourceMetaData = sourceMetaData;

		// Bind the task administrator in registry
		REGISTRY.put(name, state);

		// Return the builder
		return (AdministrationBuilder<A>) builder;
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
	 * ====================== AdministratorSource ============================
	 */

	@Override
	public AdministratorSourceSpecification getSpecification() {
		throw new UnsupportedOperationException("Not supported by mock implementation");
	}

	@Override
	public void init(AdministratorSourceContext context) throws Exception {
		// Obtain the Task Administrator Source State
		String name = context.getProperty(TASK_ADMINISTRATOR_PROPERTY);
		this.taskAdministratorSourceState = REGISTRY.get(name);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AdministratorSourceMetaData getMetaData() {
		return this.taskAdministratorSourceState.taskAdministratorSourceMetaData;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Administration createAdministrator() {
		return this.taskAdministratorSourceState.taskAdministrator;
	}

	/**
	 * State of the {@link AdministratorSource} .
	 */
	private static class TaskAdministratorSourceState {

		/**
		 * {@link AdministrationDuty}.
		 */
		Administration<?, ?> taskAdministrator;

		/**
		 * {@link AdministratorSourceMetaData}.
		 */
		AdministratorSourceMetaData<?, ?> taskAdministratorSourceMetaData;
	}

}