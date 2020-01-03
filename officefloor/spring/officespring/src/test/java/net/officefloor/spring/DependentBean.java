package net.officefloor.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dependent bean for testing.
 * 
 * @author Daniel Sagenschneider
 */
@Component
public class DependentBean {

	@Autowired
	private OfficeFloorManagedObject managedObject;

	public OfficeFloorManagedObject getManagedObject() {
		return this.managedObject;
	}
}