package net.officefloor.tutorial.cosmosdbhttpserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.nosql.cosmosdb.CosmosPartitionKey;
import net.officefloor.web.HttpObject;

/**
 * Post.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

	private String id;

	private String message;

	@CosmosPartitionKey
	public String getPartitionKey() {
		return "same";
	}
}
// END SNIPPET: tutorial