package net.officefloor.tutorial.springhttpserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Hello Spring bean.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Component
public class HelloBean {

	@Autowired
	private Other other;

	public String getIntroduction() {
		return "Hello " + this.other.getName() + ", from Spring";
	}
}
// END SNIPPET: tutorial