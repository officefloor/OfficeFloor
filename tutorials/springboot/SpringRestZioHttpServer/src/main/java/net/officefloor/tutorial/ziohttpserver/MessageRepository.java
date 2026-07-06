package net.officefloor.tutorial.ziohttpserver;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

// START SNIPPET: tutorial
@Repository
public interface MessageRepository extends CrudRepository<Message, Integer> {
}
// END SNIPPET: tutorial
