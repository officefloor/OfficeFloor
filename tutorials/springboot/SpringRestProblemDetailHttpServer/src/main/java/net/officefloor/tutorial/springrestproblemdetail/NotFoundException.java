package net.officefloor.tutorial.springrestproblemdetail;

// START SNIPPET: tutorial
public class NotFoundException extends Exception {

	private final long id;

	public NotFoundException(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
}
// END SNIPPET: tutorial
