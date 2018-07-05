/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.team.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceProperty;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;

/**
 * Abstract {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTeamSource implements TeamSource {

	/*
	 * ==================== TeamSource ================================
	 */

	@Override
	public TeamSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the loaded specification
		return specification;
	}

	/**
	 * Overridden to load specification.
	 * 
	 * @param context
	 *            {@link SpecificationContext}.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for {@link #loadSpecification(SpecificationContext)}.
	 */
	public static interface SpecificationContext {

		/**
		 * Adds a property.
		 * 
		 * @param name
		 *            Name of property that is also used as the label.
		 */
		void addProperty(String name);

		/**
		 * Adds a property.
		 * 
		 * @param name
		 *            Name of property.
		 * @param label
		 *            Label for the property.
		 */
		void addProperty(String name, String label);

		/**
		 * Adds a property.
		 * 
		 * @param property
		 *            {@link TeamSourceProperty}.
		 */
		void addProperty(TeamSourceProperty property);
	}

	/**
	 * Specification for the {@link TeamSource}.
	 */
	private class Specification implements SpecificationContext,
			TeamSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<TeamSourceProperty> properties = new LinkedList<TeamSourceProperty>();

		/*
		 * ================ SpecificationContext ======================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new TeamSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new TeamSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(TeamSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ================= TeamSourceSpecification ======================
		 */

		@Override
		public TeamSourceProperty[] getProperties() {
			return this.properties.toArray(new TeamSourceProperty[0]);
		}
	}

}