package net.officefloor.tutorial.springrestproblemdetail;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
/**
 * Throws an unexpected error, to show the catch-all handler. Any exception with
 * no more specific handler maps to a 500 Problem Detail that does not leak the
 * internal message.
 */
public class Boom {

	public void service(ObjectResponse<ArticleResponse> response) {
		throw new IllegalStateException("database connection pool exhausted");
	}
}
// END SNIPPET: tutorial
