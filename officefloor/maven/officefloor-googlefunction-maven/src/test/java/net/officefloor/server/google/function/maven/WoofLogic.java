package net.officefloor.server.google.function.maven;

import java.io.IOException;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * {@link Woof} logic for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLogic {

	/**
	 * Simple response.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 * @throws IOException If fails to write response.
	 */
	public void service(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().append("SERVICED");
	}

	/**
	 * Create {@link Firestore} entry.
	 * 
	 * @param post      {@link Post} to create.
	 * @param firestore {@link Firestore}.
	 * @param response  {@link ObjectResponse}.
	 * @throws Exception If fails to create entry.
	 */
	public void create(Post post, Firestore firestore, ObjectResponse<Post> response) throws Exception {
		DocumentReference docRef = firestore.collection(Post.class.getSimpleName()).document();
		Post entity = new Post(docRef.getId(), post.getMessage());
		docRef.create(entity).get();
		response.send(entity);
	}

	/**
	 * Retrieve {@link Firestore} entry.
	 * 
	 * @param identifier Identifier of {@link Post}.
	 * @param response   {@link ObjectResponse}.
	 * @param firestore  {@link Firestore}.
	 * @throws Exception If fails to create entry.
	 */
	public void retrieve(@HttpPathParameter("id") String identifier, ObjectResponse<Post> response, Firestore firestore)
			throws Exception {
		Post post = firestore.collection(Post.class.getSimpleName()).document(identifier).get().get()
				.toObject(Post.class);
		response.send(post);
	}

}
