/*-
 * #%L
 * OfficeFloor Filing Cabinet for Dynamo DB
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.cabinet.dynamo;

import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import net.officefloor.cabinet.AbstractOfficeCabinetTest;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.domain.DomainCabinetManufacturerImpl;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link DynamoOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class DynamoOfficeCabinetTest extends AbstractOfficeCabinetTest {

	public static @RegisterExtension final DynamoDbExtension dynamoDb = new DynamoDbExtension();

	/*
	 * ================== AbstractOfficeCabinetTest =================
	 */

	@Override
	protected OfficeStore getOfficeStore() {
		AmazonDynamoDB amazonDynamoDb = dynamoDb.getAmazonDynamoDb();
		return new DynamoOfficeStore(amazonDynamoDb);
	}

	@Override
	protected DomainCabinetManufacturer getDomainSpecificCabinetManufacturer() {
		return new DomainCabinetManufacturerImpl(this.getClass().getClassLoader());
	}

}