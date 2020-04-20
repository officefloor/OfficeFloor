package net.officefloor.tutorial.springcontrollerhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestModel {

	private String input;
}
