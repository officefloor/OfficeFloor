package net.officefloor.tutorial.springrestdatajpa;

// START SNIPPET: tutorial
public class ArticleNotFoundException extends Exception {

	private final long id;

	public ArticleNotFoundException(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
}
// END SNIPPET: tutorial
