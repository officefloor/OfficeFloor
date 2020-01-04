package net.officefloor.tutorial.resthttpserver;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Vehicle.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Entity
@HttpObject
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NonNull
	@Column(name = "VEHICLE_TYPE")
	private String vehicleType;

	@NonNull
	private Integer wheels;
}
// END SNIPPET: tutorial