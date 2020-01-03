package net.officefloor.web.build;

/**
 * Parameter within the HTTP content.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpContentParametersBuilder {

	HttpParameterBuilder addParameter(String name);

}