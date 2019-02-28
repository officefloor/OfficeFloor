package net.officefloor.tutorial.objectifyhttpserver;

import java.util.List;

import com.googlecode.objectify.Objectify;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * {@link Objectify} logic.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyLogic {

	public void savePost(Post post, Objectify objectify) {
		objectify.save().entities(post).now();
	}

	public void retrieveAllPosts(Objectify objectify, ObjectResponse<List<Post>> response) {
		List<Post> posts = objectify.load().type(Post.class).list();
		response.send(posts);
	}

	public void retrievePost(@HttpPathParameter("id") String identifier, Objectify objectify,
			ObjectResponse<Post> response) {
		Post post = objectify.load().type(Post.class).id(Long.parseLong(identifier)).now();
		response.send(post);
	}

}