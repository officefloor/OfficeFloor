package net.officefloor.cabinet.dynamo;

import org.junit.jupiter.api.extension.RegisterExtension;

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

	@RegisterExtension
	public final DynamoDbExtension dynamoDb = new DynamoDbExtension();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeCabinet<AttributeTypesEntity> getAttributeTypesOfficeCabinet() {
		return new DynamoOfficeCabinet<>(this.dynamoDb.getAmazonDynamoDb());
	}

}