package net.officefloor.tutorial.swaggerhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Request.
 * 
 * @author Daniel Sagenschneider
 */
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {

	private Integer id;
}