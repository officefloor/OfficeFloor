package net.officefloor.nosql.dynamodb;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.test.system.EnvironmentExtension;
import net.officefloor.test.system.SystemPropertiesExtension;

/**
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbTest {

	@RegisterExtension
	public final EnvironmentExtension environment = new EnvironmentExtension("AWS_ACCESS_KEY", "LOCAL",
			"AWS_SECRET_KEY", "LOCAL");

	@RegisterExtension
	public final SystemPropertiesExtension systemProperties = new SystemPropertiesExtension("sqlite4java.library.path",
			"target/native-libs");

	@Test
	public void runEmbedded() throws Exception {
		AmazonDynamoDBLocal dynamoLocal = DynamoDBEmbedded.create();
		try {
			AmazonDynamoDB dynamo = dynamoLocal.amazonDynamoDB();
			createTableViaLowLevel(dynamo);
			createTableViaHighLevel(dynamo);
			listTables(dynamo.listTables(), "Embedded");

		} finally {
			dynamoLocal.shutdown();
		}
	}

	@Test
	public void runWithHttp() throws Exception {
		DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(new String[] { "-inMemory" });
		try {
			server.start();

			AmazonDynamoDB dynamo = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
					new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2")).build();
			createTableViaLowLevel(dynamo);
			createTableViaHighLevel(dynamo);
			listTables(dynamo.listTables(), "HTTP");

		} finally {
			server.stop();
		}
	}

	public static void createTableViaLowLevel(AmazonDynamoDB db) throws Exception {
		DynamoDB dynamo = new DynamoDB(db);

		Table table = dynamo.createTable("low_level",
				Arrays.asList(new KeySchemaElement("year", KeyType.HASH), new KeySchemaElement("title", KeyType.RANGE)),
				Arrays.asList(new AttributeDefinition("year", ScalarAttributeType.N),
						new AttributeDefinition("title", ScalarAttributeType.S)),
				new ProvisionedThroughput(10L, 10L));
		table.waitForActive();
	}

	public static void createTableViaHighLevel(AmazonDynamoDB db) throws Exception {
		DynamoDBMapper dynamo = new DynamoDBMapper(db);
		new DynamoDB(db).createTable(dynamo.generateCreateTableRequest(TestEntity.class)
				.withProvisionedThroughput(new ProvisionedThroughput(10L, 10L))).waitForActive();
		dynamo.save(new TestEntity(1, "TEST"));
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@DynamoDBTable(tableName = "test_entity")
	public static class TestEntity {

		@DynamoDBHashKey
		private Integer id;

		private String message;
	}

	public static void listTables(ListTablesResult result, String method) {
		System.out.println("Tables for " + method + ":");
		for (String table : result.getTableNames()) {
			System.out.println("\t" + table);
		}
	}
}
