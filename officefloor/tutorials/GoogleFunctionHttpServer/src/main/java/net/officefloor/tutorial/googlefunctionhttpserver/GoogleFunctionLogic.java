package net.officefloor.tutorial.googlefunctionhttpserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
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
		Map<String, Object> data = new HashMap<>();
		data.put("message", post.message);
		docRef.set(data).get();

		// Response with entity
		response.send(new PostEntity(docRef.getId(), post.message));
	}

	public void getPost(@HttpPathParameter("id") String identifier, Firestore firestore,
			ObjectResponse<PostEntity> response) throws ExecutionException, InterruptedException {

		// Obtain the entity
		DocumentSnapshot document = firestore.collection(PostEntity.class.getSimpleName()).document(identifier).get()
				.get();

		// Response with entity
		response.send(new PostEntity(document.getId(), document.getString("message")));
	}

	public void getPosts(Firestore firestore, ObjectResponse<PostEntity[]> response)
			throws InterruptedException, ExecutionException {

		// Load the entities
		List<PostEntity> entities = new ArrayList<>();
		for (DocumentReference docRef : firestore.collection(PostEntity.class.getSimpleName()).listDocuments()) {
			DocumentSnapshot document = docRef.get().get();
			entities.add(new PostEntity(docRef.getId(), document.getString("message")));
		}

		// Respond with entities
		response.send(entities.toArray(PostEntity[]::new));
	}

}