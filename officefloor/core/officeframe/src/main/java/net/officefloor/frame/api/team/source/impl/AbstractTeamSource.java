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