package net.officefloor.frame.api.build;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;

/**
 * Builder of {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveBuilder<TS extends ExecutiveSource> {

	/**
	 * Specifies a property for the {@link ExecutiveSource}.
	 * 
	 * @param name  Name of property.
	 * @param value Value of property.
	 */
	void addProperty(String name, String value);

}