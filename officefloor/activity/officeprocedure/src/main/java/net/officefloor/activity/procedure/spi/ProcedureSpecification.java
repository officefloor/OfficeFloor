package net.officefloor.activity.procedure.spi;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureProperty;

/**
 * Builds the specification for the {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureSpecification {

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
	 * @param property {@link ProcedureProperty}.
	 */
	void addProperty(ProcedureProperty property);

}