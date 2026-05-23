package net.officefloor.tutorial.dynamodbhttpserver;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * {@link DynamoDBMapper} logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class DynamoDbLogic {

	public void savePost(Post post, DynamoDBMapper dynamo) {
		dynamo.save(post);
	}

	public void retrievePost(@HttpPathParameter("id") String identifier, DynamoDBMapper dynamo,
			ObjectResponse<Post> response) {
		Post post = dynamo.load(Post.class, identifier);
		response.send(post);
	}

	public void retrieveAllPosts(DynamoDBMapper dynamo, ObjectResponse<Post[]> response) {
		PaginatedScanList<Post> page = dynamo.scan(Post.class, new DynamoDBScanExpression());
		response.send(page.toArray(new Post[1]));
	}
}
// END SNIPPET: tutorial