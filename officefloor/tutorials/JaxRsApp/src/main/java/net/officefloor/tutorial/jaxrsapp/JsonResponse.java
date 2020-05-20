package net.officefloor.tutorial.jaxrsapp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON response.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonResponse {

	private String message;
}