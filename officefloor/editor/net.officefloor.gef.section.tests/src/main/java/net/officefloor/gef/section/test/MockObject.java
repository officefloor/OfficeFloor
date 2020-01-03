package net.officefloor.gef.section.test;

import net.officefloor.gef.section.SectionEditor;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * Mock object for testing the {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObject {

	@Dependency
	private Object dependency;

	private String value = "mock";

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}