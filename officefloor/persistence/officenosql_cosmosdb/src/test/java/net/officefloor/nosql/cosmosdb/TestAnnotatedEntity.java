package net.officefloor.nosql.cosmosdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Test partition entity.
 * 
 * @author Daniel Sagenschneider
 */
@CosmosEntity(containerId = "TEST_ANNOTATED_ENTITY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAnnotatedEntity implements TestEntity {

	private String id;

	private String message;

	@CosmosPartitionKey
	public String getPartitionKey() {
		return "ANNOTATED_PARTITION";
	}
}