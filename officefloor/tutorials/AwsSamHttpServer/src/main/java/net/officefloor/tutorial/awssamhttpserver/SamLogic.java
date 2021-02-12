package net.officefloor.tutorial.awssamhttpserver;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * Logic for the AWS SAM HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class SamLogic {

	@HttpObject
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Post {
		private String message;
	}

	public void createPost(Post post, DynamoDBMapper mapper, ObjectResponse<PostEntity> response) {
		PostEntity entity = new PostEntity(null, post.message);
		mapper.save(entity);
		response.send(entity);
	}

	public void getPost(@HttpPathParameter("id") String identifier, DynamoDBMapper mapper,
			ObjectResponse<PostEntity> response) {
		PostEntity entity = mapper.load(PostEntity.class, identifier);
		response.send(entity);
	}

}