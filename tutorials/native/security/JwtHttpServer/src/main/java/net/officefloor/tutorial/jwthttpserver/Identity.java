package net.officefloor.tutorial.jwthttpserver;

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