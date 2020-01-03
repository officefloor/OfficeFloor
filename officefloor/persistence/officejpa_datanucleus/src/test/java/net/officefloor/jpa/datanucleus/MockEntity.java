package net.officefloor.jpa.datanucleus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Mock {@link Entity}.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@Entity
public class MockEntity implements IMockEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	private String description;

}