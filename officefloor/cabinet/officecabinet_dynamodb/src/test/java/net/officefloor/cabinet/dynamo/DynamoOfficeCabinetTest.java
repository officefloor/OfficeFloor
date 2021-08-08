/*-
 * #%L
 * OfficeFloor Filing Cabinet for Dynamo DB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.cabinet.dynamo;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.AttributeTypesEntity;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link DynamoOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
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
