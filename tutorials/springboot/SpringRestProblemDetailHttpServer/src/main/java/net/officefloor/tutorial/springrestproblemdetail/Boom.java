package net.officefloor.tutorial.springrestproblemdetail;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class Boom {

	public void service(ObjectResponse<ArticleResponse> response) {
		throw new IllegalStateException("database connection pool exhausted");
	}
}
// END SNIPPET: tutorial
