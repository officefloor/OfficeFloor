package net.officefloor.tutorial.springrestteam;

// START SNIPPET: tutorial
public class RequestThreadService {

	public String captureThread() {
		return Thread.currentThread().getName();
	}
}
// END SNIPPET: tutorial
