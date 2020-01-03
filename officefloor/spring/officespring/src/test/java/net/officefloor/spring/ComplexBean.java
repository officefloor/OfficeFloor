package net.officefloor.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Complex bean for testing.
 * 
 * @author Daniel Sagenschneider
 */
@Component
public class ComplexBean {

	@Autowired
	private SimpleBean simpleBean;

	public SimpleBean getSimpleBean() {
		return this.simpleBean;
	}

}