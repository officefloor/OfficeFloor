package net.officefloor.tutorial.dipojohttpserver.setter;

/**
 * Logic for the <code>template.woof.html</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class SetterInjectedTemplateLogic {

	public SetterInjectedPojo getTemplateData(SetterInjectedPojo dependency) {
		return dependency;
	}
	
}
// END SNIPPET: tutorial