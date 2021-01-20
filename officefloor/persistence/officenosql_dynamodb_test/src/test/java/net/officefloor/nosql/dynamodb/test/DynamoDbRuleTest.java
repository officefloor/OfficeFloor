/*-
 * #%L
 * DynamoDB Persistence Testing
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

package net.officefloor.nosql.dynamodb.test;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.test.skip.SkipJUnit4;

/**
 * Tests the {@link DynamoDbRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoDbRuleTest extends AbstractDynamoDbTestCase {

	@Rule
	public final DynamoDbRule dynamo = new DynamoDbRule();

	@Test
	public void test() throws Exception {
		SkipJUnit4.skipDocker();
		this.doTest(this.dynamo.getAmazonDynamoDb());
	}

}
