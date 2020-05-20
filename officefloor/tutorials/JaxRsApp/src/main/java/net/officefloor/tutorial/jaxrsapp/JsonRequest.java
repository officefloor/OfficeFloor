package net.officefloor.tutorial.jaxrsapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON request.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonRequest {

	private String input;
}