package net.officefloor.tutorial.firestorehttpserver;

import java.util.List;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * {@link Firestore} logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class FirestoreLogic {

	public void savePost(Post post, Firestore firestore) throws Exception {
		DocumentReference docRef = firestore.collection(Post.class.getSimpleName()).document();
		docRef.create(new Post(docRef.getId(), post.getMessage())).get();
	}

	public void retrievePost(@HttpPathParameter("id") String identifier, Firestore firestore,
			ObjectResponse<Post> response) throws Exception {
		Post post = firestore.collection(Post.class.getSimpleName()).document(identifier).get().get()
				.toObject(Post.class);
		response.send(post);
	}

	public void retrieveAllPosts(Firestore firestore, ObjectResponse<Post[]> response) throws Exception {
		List<QueryDocumentSnapshot> documents = firestore.collection(Post.class.getSimpleName()).get().get()
				.getDocuments();
		Post[] posts = documents.stream().map((document) -> document.toObject(Post.class)).toArray(Post[]::new);
		response.send(posts);
	}
}
// END SNIPPET: tutorial