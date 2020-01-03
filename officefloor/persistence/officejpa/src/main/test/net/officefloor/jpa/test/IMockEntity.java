package net.officefloor.jpa.test;

import javax.persistence.Entity;

/**
 * Mock {@link Entity} to allow different {@link Entity} implementations for
 * each implementing vendor.
 * 
 * @author Daniel Sagenschneider
 */
public interface IMockEntity {

	/**
	 * Obtains the identifier.
	 * 
	 * @return Identifier.
	 */
	Long getId();

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	String getName();

	/**
	 * Specifies the name.
	 * 
	 * @param name
	 *            Name.
	 */
	void setName(String name);

	/**
	 * Obtains the description.
	 * 
	 * @return Description.
	 */
	String getDescription();

	/**
	 * Specifies the description.
	 * 
	 * @param description
	 *            Description.
	 */
	void setDescription(String description);

}