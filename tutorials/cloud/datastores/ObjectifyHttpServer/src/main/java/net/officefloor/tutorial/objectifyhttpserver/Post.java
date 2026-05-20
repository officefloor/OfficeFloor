package net.officefloor.tutorial.objectifyhttpserver;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

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
@Entity
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {

	@Id
	private Long id;

	private String message;
}
// END SNIPPET: tutorial