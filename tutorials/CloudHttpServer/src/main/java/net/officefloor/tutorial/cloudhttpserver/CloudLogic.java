package net.officefloor.tutorial.cloudhttpserver;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;

/**
 * Cloud Logic.
 * 
 * @author Daniel Sagenschneider
 */
public class CloudLogic {

	public void retrieve(@HttpPathParameter("key") String key, PostRepository repository,
			ObjectResponse<Post> response) {
		Post post = repository.getPostByKey(key);
		response.send(post);
	}

	public void store(Post post, PostRepository repository, @HttpResponse(status = 204) ObjectResponse<Post> response) {
		repository.store(post);
		response.send(post);
	}
}
