package net.officefloor.spring;

import org.springframework.stereotype.Component;

/**
 * Simple bean for testing.
 * 
 * @author Daniel Sagenschneider
 */
@Component
public class SimpleBean {

	public String getValue() {
		return "SIMPLE";
	}
}