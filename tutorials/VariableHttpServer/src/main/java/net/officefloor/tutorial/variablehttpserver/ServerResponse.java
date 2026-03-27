package net.officefloor.tutorial.variablehttpserver;

import lombok.Value;

/**
 * Response object from server.
 * 
 * @author Daniel Sagenschneider
 */
@Value
public class ServerResponse {
	private Person person;
	private String description;
}