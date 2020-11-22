package net.officefloor.tutorial.googleappenginehttpserver;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class Post {

	@Id
	private Long id;

	private String message;
}
// END SNIPPET: tutorial