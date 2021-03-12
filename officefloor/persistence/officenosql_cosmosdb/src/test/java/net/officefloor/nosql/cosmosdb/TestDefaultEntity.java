package net.officefloor.nosql.cosmosdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Test configured entity.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDefaultEntity implements TestEntity {

	private String id;

	private String message;

}