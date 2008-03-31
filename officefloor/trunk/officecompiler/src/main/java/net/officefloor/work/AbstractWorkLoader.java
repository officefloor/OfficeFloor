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
package net.officefloor.work;

import java.util.LinkedList;
import java.util.List;

/**
 * Abstract {@link WorkLoader}.
 * 
 * @author Daniel
 */
public abstract class AbstractWorkLoader implements WorkLoader {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.WorkLoader#getSpecification()
	 */
	@Override
	public WorkSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the specification
		return specification;
	}

	/**
	 * Loads the {@link WorkSpecification}.
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
		 *            {@link WorkProperty}.
		 */
		void addProperty(WorkProperty property);

	}

	/**
	 * {@link SpecificationContext} providing the detail of the
	 * {@link WorkSpecification}.
	 */
	private static class Specification implements SpecificationContext,
			WorkSpecification {

		/**
		 * {@link WorkProperty} instances.
		 */
		private final List<WorkProperty> properties = new LinkedList<WorkProperty>();

		/*
		 * ======================================================================
		 * SpecificationContext
		 * ======================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.work.AbstractWorkLoader.SpecificationContext#addProperty(java.lang.String)
		 */
		@Override
		public void addProperty(String name) {
			this.properties.add(new WorkPropertyImpl(name, null));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.work.AbstractWorkLoader.SpecificationContext#addProperty(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new WorkPropertyImpl(name, label));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.work.AbstractWorkLoader.SpecificationContext#addProperty(net.officefloor.work.WorkProperty)
		 */
		@Override
		public void addProperty(WorkProperty property) {
			this.properties.add(property);
		}

		/*
		 * ======================================================================
		 * WorkSpecification
		 * ======================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.work.WorkSpecification#getProperties()
		 */
		@Override
		public WorkProperty[] getProperties() {
			return this.properties.toArray(new WorkProperty[0]);
		}

	}

	/**
	 * {@link WorkProperty} implementation.
	 */
	private static class WorkPropertyImpl implements WorkProperty {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Label.
		 */
		private final String label;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name.
		 * @param label
		 *            Label. Defaults to <code>name</code> if
		 *            <code>null</code>.
		 */
		public WorkPropertyImpl(String name, String label) {
			this.name = name;
			this.label = ((label == null) || (label.trim().length() == 0)) ? name
					: label;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.work.WorkProperty#getLabel()
		 */
		@Override
		public String getLabel() {
			return this.label;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.work.WorkProperty#getName()
		 */
		@Override
		public String getName() {
			return this.name;
		}

	}

	// loadWork to be implemented
}
