package net.officefloor.tutorial.sessionhttpserver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpSessionStateful;

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

	public Posts getTemplateData(Posts posts) {
		return posts;
	}

	public void post(Post post, Posts posts) {
		posts.addPost(post);
	}

}
// END SNIPPET: example