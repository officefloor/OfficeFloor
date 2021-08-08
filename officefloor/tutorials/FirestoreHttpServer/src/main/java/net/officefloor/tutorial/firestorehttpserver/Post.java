package net.officefloor.tutorial.firestorehttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Post.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {

	private String id;

	private String message;
}
// END SNIPPET: tutorial