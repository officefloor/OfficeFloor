package net.officefloor.nosql.firestore;

import com.google.cloud.firestore.Firestore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Test entity for {@link Firestore}.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestEntity {

	private String firstName;

	private String lastName;

	private int level;
}
