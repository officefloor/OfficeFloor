package net.officefloor.cabinet.dynamo;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesEntity;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;

/**
 * Tests the {@link DynamoOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeCabinetTest extends AbstractOfficeCabinetTest {

	public @RegisterExtension static final DynamoDbExtension dynamoDb = new DynamoDbExtension();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeCabinet<AttributeTypesEntity> getAttributeTypesOfficeCabinet() throws Exception {
		AmazonDynamoDB amazonDynamoDb = dynamoDb.getAmazonDynamoDb();
		return new DynamoOfficeCabinet<>(AttributeTypesEntity.class, new DynamoDB(amazonDynamoDb));
	}

}