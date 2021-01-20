/*-
 * #%L
 * DynamoDB Connect
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

package net.officefloor.nosql.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

/**
 * Factory for {@link AmazonDynamoDB} connection.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface AmazonDynamoDbFactory {

	/**
	 * Creates the {@link AmazonDynamoDB}.
	 * 
	 * @return {@link AmazonDynamoDB}.
	 * @throws Exception If fails to create {@link AmazonDynamoDB}.
	 */
	AmazonDynamoDB createAmazonDynamoDB() throws Exception;

}
