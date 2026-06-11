package net.officefloor.tutorial.sessionhttpserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpSessionStateful;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	@HttpSessionStateful
	public static class Posts implements Serializable {
		private static final long serialVersionUID = 1L;

		private final List<Post> posts = new ArrayList<Post>();

		public void addPost(Post post) {
			this.posts.add(post);
		}

		public Post[] getPosts() {
			return this.posts.toArray(new Post[this.posts.size()]);
		}
	}

	@Data
	@HttpParameters
	public static class Post implements Serializable {
		private static final long serialVersionUID = 1L;

		private String text;
	}

	/**
	 * Provides values from the HTTP session.
	 * 
	 * @param posts {@link Posts} being the session bound object.
	 * @return {@link Posts} to contain values to render to page.
	 */
	public Posts getTemplateData(Posts posts) {
		return posts;
	}

	/**
	 * Handles the post form submission.
	 * 
	 * @param post  {@link Post} that is dependency injected with HTTP parameters
	 *              loaded onto it.
	 * @param posts {@link Posts} that is dependency injected from the HTTP session.
	 */
	public void post(Post post, Posts posts) {
		posts.addPost(post);
	}

}
// END SNIPPET: example