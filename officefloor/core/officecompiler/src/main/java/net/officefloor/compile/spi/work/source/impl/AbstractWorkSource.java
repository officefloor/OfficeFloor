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
package net.officefloor.compile.spi.work.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceProperty;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.frame.api.execute.Work;

/**
 * Abstract {@link WorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWorkSource<W extends Work> implements
		WorkSource<W> {

	/*
	 * =============== WorkSource ==================================
	 */

	@Override
	public WorkSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the specification
		return specification;
	}

	/**
	 * Loads the {@link WorkSourceSpecification}.
	 * 
	 * @param context
	 *            {@link SpecificationContext}.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for defining the specification.
	 */
	protected interface SpecificationContext {

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
		 *            {@link WorkSourceProperty}.
		 */
		void addProperty(WorkSourceProperty property);

	}

	/**
	 * {@link SpecificationContext} providing the detail of the
	 * {@link WorkSourceSpecification}.
	 */
	private static class Specification implements SpecificationContext,
			WorkSourceSpecification {

		/**
		 * {@link WorkSourceProperty} instances.
		 */
		private final List<WorkSourceProperty> properties = new LinkedList<WorkSourceProperty>();

		/*
		 * ================== SpecificationContext ===========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new WorkSourcePropertyImpl(name, null));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new WorkSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(WorkSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * =================== WorkSpecification ==============================
		 */

		@Override
		public WorkSourceProperty[] getProperties() {
			return this.properties.toArray(new WorkSourceProperty[0]);
		}
	}

	/**
	 * <code>sourceWork</code> to be implemented.
	 */

}