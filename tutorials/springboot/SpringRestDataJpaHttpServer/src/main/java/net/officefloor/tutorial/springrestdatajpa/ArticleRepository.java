package net.officefloor.tutorial.springrestdatajpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// START SNIPPET: tutorial
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
// END SNIPPET: tutorial
