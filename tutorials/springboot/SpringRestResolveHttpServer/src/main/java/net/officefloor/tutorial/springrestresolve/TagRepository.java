package net.officefloor.tutorial.springrestresolve;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

// START SNIPPET: tutorial
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

	List<Tag> findByNameIn(Collection<String> names);
}
// END SNIPPET: tutorial
