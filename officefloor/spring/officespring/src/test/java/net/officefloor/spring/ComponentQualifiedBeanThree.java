package net.officefloor.spring;

import org.springframework.stereotype.Component;

/**
 * Component qualified bean.
 * 
 * @author Daniel Sagenschneider
 */
@Component("qualifiedThree")
public class ComponentQualifiedBeanThree extends QualifiedBean {

	public ComponentQualifiedBeanThree() {
		super("Three");
	}

}
