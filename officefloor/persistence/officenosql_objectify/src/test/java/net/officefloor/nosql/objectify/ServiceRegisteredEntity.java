package net.officefloor.nosql.objectify;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * {@link ObjectifyEntityLocatorServiceFactory} located {@link Entity}.
 * 
 * @author Daniel Sagenschneider
 */
@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ServiceRegisteredEntity {

	@Id
	private Long id;

	private String test;

}