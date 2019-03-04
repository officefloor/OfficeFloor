package net.officefloor.nosql.objectify;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Located {@link Entity} via configured {@link ObjectifyEntityLocator}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class LocatedEntity {

	@Id
	private Long id;

	private String location;

}