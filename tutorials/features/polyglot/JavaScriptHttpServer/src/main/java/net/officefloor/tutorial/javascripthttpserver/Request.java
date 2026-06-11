package net.officefloor.tutorial.javascripthttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Request to be validated.
 * 
 * @author Daniel Sagenschneider
 */
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
	private int id;
	private String name;
}