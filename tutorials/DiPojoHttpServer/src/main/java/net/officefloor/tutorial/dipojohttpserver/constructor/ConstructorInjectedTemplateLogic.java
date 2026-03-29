package net.officefloor.tutorial.dipojohttpserver.constructor;

/**
 * Logic for the <code>template.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ConstructorInjectedTemplateLogic {

	public ConstructorInjectedPojo getTemplateData(ConstructorInjectedPojo dependency) {
		return dependency;
	}
	
}
// END SNIPPET: tutorial