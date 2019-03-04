package net.officefloor.nosql.objectify;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Mock {@link Entity}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class MockEntity {

	@Id
	private Long id;

	private String stringValue;

	@Index
	private String indexedStringValue;

	private Integer integerValue;

	@Index
	private Integer indexedIntegerValue;

}