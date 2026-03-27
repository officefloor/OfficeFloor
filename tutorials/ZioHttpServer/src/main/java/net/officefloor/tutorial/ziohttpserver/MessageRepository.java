package net.officefloor.tutorial.ziohttpserver;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link Repository} for {@link Message}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Repository
public interface MessageRepository extends CrudRepository<Message, Integer> {
}
// END SNIPPET: tutorial