package net.officefloor.tutorial.variablehttpserver;

import lombok.Value;

@Value
public class ServerResponse {
	private Person person;
	private String description;
}