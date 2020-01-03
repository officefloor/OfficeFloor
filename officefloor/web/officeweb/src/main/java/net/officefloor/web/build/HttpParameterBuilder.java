package net.officefloor.web.build;

/**
 *
 * @author Daniel Sagenschneider
 */
public interface HttpParameterBuilder {

	void setRequired(boolean isRequired);
	
	void setAlias(String aliasName);

}