package net.officefloor.tutorial.transactionhttpserver;

import java.io.EOFException;

import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Transaction logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class TransactionLogic {

	public IllegalArgumentException rollback(Post post, PostRepository repository) {
		repository.save(post);
		return new IllegalArgumentException("rolled back");
	}

	public EOFException commit(Post post, PostRepository repository) throws EOFException {
		repository.save(post);
		return new EOFException("committed");
	}

	public void fail(@Parameter Exception failure, PostRepository repository, TeamMarkerBean marker) throws Exception {
		repository.save(new Post(null, "Additional"));
		throw failure;
	}

}
// END SNIPPET: tutorial