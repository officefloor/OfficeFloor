package net.officefloor.tutorial.jwtresourcehttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * JWT claims.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Claims {
	private String id;
	private String[] roles;
}