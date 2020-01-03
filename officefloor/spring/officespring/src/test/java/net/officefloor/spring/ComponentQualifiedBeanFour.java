package net.officefloor.spring;

import org.springframework.stereotype.Component;

/**
 * Component qualified bean.
 * 
 * @author Daniel Sagenschneider
 */
@Component("qualifiedFour")
public class ComponentQualifiedBeanFour extends QualifiedBean {

	public ComponentQualifiedBeanFour() {
		super("Four");
	}

}