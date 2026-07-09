package net.officefloor.tutorial.springrestfilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// START SNIPPET: tutorial
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

	// Derived finders for the optional category filter.
	List<Article> findByCategory(String category);

	Page<Article> findByCategory(String category, Pageable pageable);
}
// END SNIPPET: tutorial
