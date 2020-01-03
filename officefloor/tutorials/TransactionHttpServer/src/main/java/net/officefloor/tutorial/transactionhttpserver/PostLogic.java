package net.officefloor.tutorial.transactionhttpserver;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.web.ObjectResponse;

/**
 * Logic for the post page.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class PostLogic {

	public void getPosts(PostRepository repository, ObjectResponse<List<Post>> responder) {
		List<Post> posts = new LinkedList<>();
		for (Post post : repository.findAll()) {
			posts.add(post);
		}
		responder.send(posts);
	}

	public void create(Post post, PostRepository repository, ObjectResponse<Post> responder) {
		repository.save(post);
		responder.send(post);
	}

}
// END SNIPPET: tutorial
