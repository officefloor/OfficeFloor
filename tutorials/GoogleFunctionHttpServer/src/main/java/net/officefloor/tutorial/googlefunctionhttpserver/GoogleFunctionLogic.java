package net.officefloor.tutorial.googlefunctionhttpserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic for the Google Function HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleFunctionLogic {

	@HttpObject
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Post {
		private String message;
	}

	public void createPost(Post post, Firestore firestore, ObjectResponse<PostEntity> response)
			throws InterruptedException, ExecutionException {

		// Store the entity
		DocumentReference docRef = firestore.collection(PostEntity.class.getSimpleName()).document();
		PostEntity entity = new PostEntity(docRef.getId(), post.getMessage());
		docRef.create(entity).get();

		// Response with entity
		response.send(entity);
	}

	public void getPost(@HttpPathParameter("id") String identifier, Firestore firestore,
			ObjectResponse<PostEntity> response) throws ExecutionException, InterruptedException {

		// Obtain the entity
		PostEntity entity = firestore.collection(PostEntity.class.getSimpleName()).document(identifier).get().get()
				.toObject(PostEntity.class);

		// Response with entity
		response.send(entity);
	}

	public void getPosts(Firestore firestore, ObjectResponse<PostEntity[]> response)
			throws InterruptedException, ExecutionException {

		// Load the entities
		List<PostEntity> entities = new ArrayList<>();
		for (DocumentReference docRef : firestore.collection(PostEntity.class.getSimpleName()).listDocuments()) {
			PostEntity entity = docRef.get().get().toObject(PostEntity.class);
			entities.add(entity);
		}

		// Respond with entities
		response.send(entities.toArray(PostEntity[]::new));
	}

}