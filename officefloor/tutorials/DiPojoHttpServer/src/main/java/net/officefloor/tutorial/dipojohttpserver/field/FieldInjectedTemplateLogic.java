package net.officefloor.tutorial.dipojohttpserver.field;

/**
 * Logic for the <code>template.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class FieldInjectedTemplateLogic {

	public FieldInjectedPojo getTemplateData(FieldInjectedPojo dependency) {
		return dependency;
	}
	
}
// END SNIPPET: tutorial