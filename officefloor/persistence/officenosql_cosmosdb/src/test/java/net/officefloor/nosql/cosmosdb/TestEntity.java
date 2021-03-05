package net.officefloor.nosql.cosmosdb;

/**
 * Test entity.
 * 
 * @author Daniel Sagenschneider
 */
@CosmosEntity(containerId = "TEST_ENTITY")
public interface TestEntity {

	String getId();

	String getMessage();

}