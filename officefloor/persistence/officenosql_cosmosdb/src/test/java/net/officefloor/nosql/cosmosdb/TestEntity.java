package net.officefloor.nosql.cosmosdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Test entity.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity {

	private final String partition = "TEST";

	private String id;

	private String message;

}