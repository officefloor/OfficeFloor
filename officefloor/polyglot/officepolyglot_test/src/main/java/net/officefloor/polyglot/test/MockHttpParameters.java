package net.officefloor.polyglot.test;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpParameters;

/**
 * Mock {@link HttpParameters} object.
 * 
 * @author Daniel Sagenschneider
 */
@HttpParameters
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MockHttpParameters implements Serializable {
	private static final long serialVersionUID = 1L;

	private String mock;
}