package net.officefloor.frame.api.executive.source.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.executive.source.ExecutiveSourceProperty;
import net.officefloor.frame.api.executive.source.ExecutiveSourceSpecification;

/**
 * Abstract {@link ExecutiveSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractExecutiveSource implements ExecutiveSource {

	/*
	 * ================== ExecutiveSource =====================
	 */

	@Override
	public ExecutiveSourceSpecification getSpecification() {
		// Create and load the specification
		Specification specification = new Specification();
		this.loadSpecification(specification);

		// Return the loaded specification
		return specification;
	}

	/**
	 * Overridden to load specification.
	 * 
	 * @param context {@link SpecificationContext}.
	 */
	protected abstract void loadSpecification(SpecificationContext context);

	/**
	 * Context for {@link #loadSpecification(SpecificationContext)}.
	 */
	public static interface SpecificationContext {

		/**
		 * Adds a property.
		 * 
		 * @param name Name of property that is also used as the label.
		 */
		void addProperty(String name);

		/**
		 * Adds a property.
		 * 
		 * @param name  Name of property.
		 * @param label Label for the property.
		 */
		void addProperty(String name, String label);

		/**
		 * Adds a property.
		 * 
		 * @param property {@link ExecutiveSourceProperty}.
		 */
		void addProperty(ExecutiveSourceProperty property);
	}

	/**
	 * Specification for the {@link ExecutiveSource}.
	 */
	private class Specification implements SpecificationContext, ExecutiveSourceSpecification {

		/**
		 * Properties for the specification.
		 */
		private final List<ExecutiveSourceProperty> properties = new LinkedList<ExecutiveSourceProperty>();

		/*
		 * ================ SpecificationContext ======================
		 */

		@Override
		public void addProperty(String name) {
			this.properties.add(new ExecutiveSourcePropertyImpl(name, name));
		}

		@Override
		public void addProperty(String name, String label) {
			this.properties.add(new ExecutiveSourcePropertyImpl(name, label));
		}

		@Override
		public void addProperty(ExecutiveSourceProperty property) {
			this.properties.add(property);
		}

		/*
		 * ================= ExecutiveSourceSpecification ======================
		 */

		@Override
		public ExecutiveSourceProperty[] getProperties() {
			return this.properties.toArray(new ExecutiveSourceProperty[0]);
		}
	}

}