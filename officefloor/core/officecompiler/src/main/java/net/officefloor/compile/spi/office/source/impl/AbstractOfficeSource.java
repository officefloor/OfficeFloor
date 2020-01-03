package net.officefloor.compile.spi.office.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceProperty;
import net.officefloor.compile.spi.office.source.OfficeSourceSpecification;

/**
 * Abstract {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeSource implements OfficeSource {

	/*
	 * =============== OfficeSource ==================================
	 */

	@Override
	public OfficeSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the specification
		return specification;
	}

	/**
	 * Loads the {@link OfficeSourceSpecification}.
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
		 *            {@link OfficeSourceProperty}.
		 */
		void addProperty(OfficeSourceProperty property);

	}

	/**
	 * {@link SpecificationContext} providing the detail of the
	 * {@link OfficeSourceSpecification}.
	 */
	private static class Specification implements SpecificationContext,
			OfficeSourceSpecification {

		/**
		 * {@link OfficeSourceProperty} instances.
		 */
		private final List<OfficeSourceProperty> properties = new LinkedList<OfficeSourceProperty>();

		/*
		 * ================== SpecificationContext ===========================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new OfficeSourcePropertyImpl(name, null));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new OfficeSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(OfficeSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * =================== OfficeSpecification ===========================
		 */

		@Override
		public OfficeSourceProperty[] getProperties() {
			return this.properties.toArray(new OfficeSourceProperty[0]);
		}
	}

	/**
	 * <code>sourceOffice</code> to be implemented.
	 */

}