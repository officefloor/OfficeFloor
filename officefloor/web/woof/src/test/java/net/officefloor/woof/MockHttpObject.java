package net.officefloor.woof;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Mock {@link HttpObject}.
 * 
 * @author Daniel Sagenschneider
 */
@HttpObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MockHttpObject {
	private String message;
}