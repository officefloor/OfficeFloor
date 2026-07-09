package net.officefloor.tutorial.springrestrelationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// START SNIPPET: tutorial
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
// END SNIPPET: tutorial
