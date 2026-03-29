package net.officefloor.tutorial.jwtauthorityhttpserver;

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
	private int randomValueToMakeAccessTokensDifferent;
	private String[] roles;
}