package net.officefloor.tutorial.jwtauthorityhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * JWT Identity.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Identity {
	private String id;
}