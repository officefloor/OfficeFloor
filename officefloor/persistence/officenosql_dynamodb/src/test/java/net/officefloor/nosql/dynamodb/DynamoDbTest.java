package net.officefloor.nosql.dynamodb;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

/**
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbTest {

	@Test
	public void runWithoutHttp() {

		AmazonDynamoDBLocal dynamoLocal = DynamoDBEmbedded.create();
		try {
			AmazonDynamoDB dynamo = dynamoLocal.amazonDynamoDB();
			listTables(dynamo.listTables(), "No HTTP");
		} finally {
			dynamoLocal.shutdown();
		}
	}

	@Test
	public void runWithHttp() throws Exception {
		DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(new String[] { "-inMemory" });
		try {
			server.start();
			
			AmazonDynamoDB dynamo = new AmazonDynamoDBAsyncClient();
			dynamo.setEndpoint("http://localhost:8000");
			listTables(dynamo.listTables(), "With HTTP");
			
		} finally {
			server.stop();
		}
	}
	
	public static void listTables(ListTablesResult result, String method) {
        System.out.println("found " + Integer.toString(result.getTableNames().size()) + " tables with " + method);
        for (String table : result.getTableNames()) {
            System.out.println(table);
        }
    }
}
