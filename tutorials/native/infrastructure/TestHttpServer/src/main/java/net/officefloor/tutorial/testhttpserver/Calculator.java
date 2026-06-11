package net.officefloor.tutorial.testhttpserver;

/**
 * Calculator dependency to show injection into tests.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class Calculator {

	/** Adds two integers together. */
	public int plus(int a, int b) {
		return a + b;
	}
}
// END SNIPPET: tutorial