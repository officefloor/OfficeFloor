package net.officefloor.tutorial.googlefunctionhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Post entity.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {

	private String id;

	private String message;

}